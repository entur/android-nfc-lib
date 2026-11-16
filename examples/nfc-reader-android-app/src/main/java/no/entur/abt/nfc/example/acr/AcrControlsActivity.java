package no.entur.abt.nfc.example.acr;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import no.entur.abt.nfc.example.MainApplication;
import no.entur.abt.nfc.example.R;
import no.entur.abt.nfc.example.utils.ParcelableExtraUtils;
import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.ExternalNfcReaderCallbackSupport;
import no.entur.android.nfc.external.acs.reader.Acr122UReader;
import no.entur.android.nfc.external.acs.reader.Acr1222LReader;
import no.entur.android.nfc.external.acs.reader.Acr1252UReader;
import no.entur.android.nfc.external.acs.reader.Acr1255UReader;
import no.entur.android.nfc.external.acs.reader.Acr1281UReader;
import no.entur.android.nfc.external.acs.reader.Acr1283LReader;
import no.entur.android.nfc.external.acs.reader.Acr1552UReader;
import no.entur.android.nfc.external.acs.reader.AcrDefaultLEDAndBuzzerBehaviour;
import no.entur.android.nfc.external.acs.reader.AcrFont;
import no.entur.android.nfc.external.acs.reader.AcrLED;
import no.entur.android.nfc.external.acs.reader.AcrReader;
import no.entur.android.nfc.wrapper.Tag;

/**
 * Try the buzzer, LED and display controls of ACS readers. Every call and its result is logged on screen and in logcat (tag AcrControls).
 */

public class AcrControlsActivity extends AppCompatActivity implements ExternalNfcReaderCallback {

	private static final String LOG_TAG = "AcrControls";

	private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.ROOT);

	private Executor executor;
	private ExternalNfcReaderCallbackSupport externalNfcReaderCallbackSupport;

	private LinearLayout container;
	private TextView log;
	private final List<String> logLines = new ArrayList<>();

	private String selectedReaderId;

	private final Map<AcrDefaultLEDAndBuzzerBehaviour, CheckBox> behaviourCheckBoxes = new EnumMap<>(AcrDefaultLEDAndBuzzerBehaviour.class);
	private final Map<AcrLED, CheckBox> ledCheckBoxes = new EnumMap<>(AcrLED.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_acr_controls);

		container = findViewById(R.id.acrControls);
		log = findViewById(R.id.acrControlsLog);

		executor = ((MainApplication) getApplication()).getThreadPoolExecutor();

		externalNfcReaderCallbackSupport = new ExternalNfcReaderCallbackSupport(this, this, executor, true);
		externalNfcReaderCallbackSupport.setEnabled(true);

		render();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// also makes the reader service announce the open reader
		externalNfcReaderCallbackSupport.onResume();

		render();
	}

	@Override
	protected void onPause() {
		super.onPause();

		externalNfcReaderCallbackSupport.onPause();
	}

	@Override
	public void onExternalNfcReaderOpened(Intent intent) {
		if (intent.hasExtra(ExternalNfcReaderCallback.EXTRA_READER_CONTROL)) {
			Parcelable reader = ParcelableExtraUtils.getParcelableExtra(intent, ExternalNfcReaderCallback.EXTRA_READER_CONTROL, Parcelable.class);
			if (reader instanceof AcrReader) {
				AcrReaders.opened((AcrReader) reader);

				runOnUiThread(this::render);
			}
		}
	}

	@Override
	public void onExternalNfcReaderClosed(Intent intent) {
		String readerId = intent.getStringExtra(ExternalNfcReaderCallback.EXTRAS_READER_ID);
		if (readerId != null) {
			AcrReaders.closed(readerId);
		}
		runOnUiThread(this::render);
	}

	@Override
	public void onTagDiscovered(Tag tag, Intent intent) {
		// tags are not handled here
	}

	private AcrReader getSelectedReader(List<AcrReader> readers) {
		for (AcrReader reader : readers) {
			if (reader.getId().equals(selectedReaderId)) {
				return reader;
			}
		}
		return readers.isEmpty() ? null : readers.get(0);
	}

	private void render() {
		container.removeAllViews();
		behaviourCheckBoxes.clear();
		ledCheckBoxes.clear();

		List<AcrReader> readers = AcrReaders.getReaders();
		AcrReader reader = getSelectedReader(readers);
		if (reader == null) {
			container.addView(text(getString(R.string.acr_controls_no_reader), false));
			return;
		}
		selectedReaderId = reader.getId();

		if (readers.size() > 1) {
			RadioGroup group = new RadioGroup(this);
			for (AcrReader r : readers) {
				RadioButton button = new RadioButton(this);
				button.setText(r.getName());
				button.setChecked(r == reader);
				button.setOnClickListener((v) -> {
					selectedReaderId = r.getId();
					render();
				});
				group.addView(button);
			}
			container.addView(group);
		} else {
			container.addView(text(reader.getName(), true));
		}

		if (hasDefaultBehaviour(reader)) {
			addDefaultBehaviourSection(reader);
		}
		if (hasLEDs(reader)) {
			addLEDSection(reader);
		}
		if (reader instanceof Acr1222LReader || reader instanceof Acr1283LReader) {
			addLightLEDSection(reader);
			addDisplaySection(reader);
		}
		addBuzzerSection(reader);
	}

	// default LED and buzzer behaviour

	private boolean hasDefaultBehaviour(AcrReader reader) {
		return reader instanceof Acr1222LReader || reader instanceof Acr1252UReader || reader instanceof Acr1255UReader || reader instanceof Acr1281UReader
				|| reader instanceof Acr1283LReader || reader instanceof Acr1552UReader;
	}

	private void addDefaultBehaviourSection(AcrReader reader) {
		container.addView(section(getString(R.string.acr_controls_default_behaviour)));
		for (AcrDefaultLEDAndBuzzerBehaviour behaviour : AcrDefaultLEDAndBuzzerBehaviour.values()) {
			CheckBox checkBox = new CheckBox(this);
			checkBox.setText(behaviour.name());
			behaviourCheckBoxes.put(behaviour, checkBox);
			container.addView(checkBox);
		}
		container.addView(row(button(getString(R.string.acr_controls_read), (v) -> run("getDefaultLEDAndBuzzerBehaviour()", () -> {
			List<AcrDefaultLEDAndBuzzerBehaviour> behaviours = getDefaultBehaviour(reader);
			runOnUiThread(() -> {
				for (Map.Entry<AcrDefaultLEDAndBuzzerBehaviour, CheckBox> entry : behaviourCheckBoxes.entrySet()) {
					entry.getValue().setChecked(behaviours.contains(entry.getKey()));
				}
			});
			return behaviours;
		})), button(getString(R.string.acr_controls_apply), (v) -> {
			AcrDefaultLEDAndBuzzerBehaviour[] types = checked(behaviourCheckBoxes).toArray(new AcrDefaultLEDAndBuzzerBehaviour[0]);
			run("setDefaultLEDAndBuzzerBehaviour(" + java.util.Arrays.toString(types) + ")", () -> setDefaultBehaviour(reader, types));
		})));
	}

	private List<AcrDefaultLEDAndBuzzerBehaviour> getDefaultBehaviour(AcrReader reader) {
		if (reader instanceof Acr1222LReader) {
			return ((Acr1222LReader) reader).getDefaultLEDAndBuzzerBehaviour();
		} else if (reader instanceof Acr1252UReader) {
			return ((Acr1252UReader) reader).getDefaultLEDAndBuzzerBehaviour();
		} else if (reader instanceof Acr1255UReader) {
			return ((Acr1255UReader) reader).getDefaultLEDAndBuzzerBehaviour();
		} else if (reader instanceof Acr1281UReader) {
			return ((Acr1281UReader) reader).getDefaultLEDAndBuzzerBehaviour();
		} else if (reader instanceof Acr1283LReader) {
			return ((Acr1283LReader) reader).getDefaultLEDAndBuzzerBehaviour();
		} else if (reader instanceof Acr1552UReader) {
			return ((Acr1552UReader) reader).getDefaultLEDAndBuzzerBehaviour();
		}
		throw new IllegalArgumentException("Not supported for " + reader.getName());
	}

	private boolean setDefaultBehaviour(AcrReader reader, AcrDefaultLEDAndBuzzerBehaviour... types) {
		if (reader instanceof Acr1222LReader) {
			return ((Acr1222LReader) reader).setDefaultLEDAndBuzzerBehaviour(types);
		} else if (reader instanceof Acr1252UReader) {
			return ((Acr1252UReader) reader).setDefaultLEDAndBuzzerBehaviour(types);
		} else if (reader instanceof Acr1255UReader) {
			return ((Acr1255UReader) reader).setDefaultLEDAndBuzzerBehaviour(types);
		} else if (reader instanceof Acr1281UReader) {
			return ((Acr1281UReader) reader).setDefaultLEDAndBuzzerBehaviour(types);
		} else if (reader instanceof Acr1283LReader) {
			return ((Acr1283LReader) reader).setDefaultLEDAndBuzzerBehaviour(types);
		} else if (reader instanceof Acr1552UReader) {
			return ((Acr1552UReader) reader).setDefaultLEDAndBuzzerBehaviour(types);
		}
		throw new IllegalArgumentException("Not supported for " + reader.getName());
	}

	// LEDs

	private boolean hasLEDs(AcrReader reader) {
		return reader instanceof Acr1222LReader || reader instanceof Acr1252UReader || reader instanceof Acr1255UReader || reader instanceof Acr1281UReader
				|| reader instanceof Acr1283LReader || reader instanceof Acr1552UReader;
	}

	private boolean canReadLEDs(AcrReader reader) {
		return reader instanceof Acr1252UReader || reader instanceof Acr1255UReader || reader instanceof Acr1281UReader || reader instanceof Acr1552UReader;
	}

	private void addLEDSection(AcrReader reader) {
		container.addView(section(getString(R.string.acr_controls_leds)));
		LinearLayout checkBoxes = new LinearLayout(this);
		checkBoxes.setOrientation(LinearLayout.HORIZONTAL);
		for (AcrLED led : AcrLED.values()) {
			CheckBox checkBox = new CheckBox(this);
			checkBox.setText(led.name());
			ledCheckBoxes.put(led, checkBox);
			checkBoxes.addView(checkBox);
		}
		container.addView(checkBoxes);

		List<View> buttons = new ArrayList<>();
		if (canReadLEDs(reader)) {
			buttons.add(button(getString(R.string.acr_controls_read), (v) -> run("getLEDs()", () -> {
				Object leds = getLEDs(reader);
				Set<AcrLED> on = EnumSet.noneOf(AcrLED.class);
				if (leds instanceof List) {
					for (Object item : (List<?>) leds) {
						if (item instanceof AcrLED) {
							on.add((AcrLED) item);
						} else if (item instanceof Set) {
							// ACR1255U: two LEDs, show the first
							if (on.isEmpty()) {
								for (Object led : (Set<?>) item) {
									on.add((AcrLED) led);
								}
							}
						}
					}
				}
				runOnUiThread(() -> {
					for (Map.Entry<AcrLED, CheckBox> entry : ledCheckBoxes.entrySet()) {
						entry.getValue().setChecked(on.contains(entry.getKey()));
					}
				});
				return leds;
			})));
		}
		buttons.add(button(getString(R.string.acr_controls_apply), (v) -> {
			AcrLED[] leds = checked(ledCheckBoxes).toArray(new AcrLED[0]);
			run("setLEDs(" + java.util.Arrays.toString(leds) + ")", () -> setLEDs(reader, leds));
		}));
		buttons.add(button(getString(R.string.acr_controls_off), (v) -> run("setLEDs()", () -> setLEDs(reader))));
		container.addView(row(buttons.toArray(new View[0])));
	}

	private Object getLEDs(AcrReader reader) {
		if (reader instanceof Acr1252UReader) {
			return ((Acr1252UReader) reader).getLEDs();
		} else if (reader instanceof Acr1255UReader) {
			return ((Acr1255UReader) reader).getLEDs();
		} else if (reader instanceof Acr1281UReader) {
			return ((Acr1281UReader) reader).getLEDs();
		} else if (reader instanceof Acr1552UReader) {
			return ((Acr1552UReader) reader).getLEDs();
		}
		throw new IllegalArgumentException("Not supported for " + reader.getName());
	}

	private boolean setLEDs(AcrReader reader, AcrLED... leds) {
		if (reader instanceof Acr1222LReader) {
			return ((Acr1222LReader) reader).setLEDs(leds);
		} else if (reader instanceof Acr1252UReader) {
			return ((Acr1252UReader) reader).setLEDs(leds);
		} else if (reader instanceof Acr1255UReader) {
			// the same for both LEDs
			Set<AcrLED> set = EnumSet.noneOf(AcrLED.class);
			java.util.Collections.addAll(set, leds);
			return ((Acr1255UReader) reader).setLEDs(set, set);
		} else if (reader instanceof Acr1281UReader) {
			return ((Acr1281UReader) reader).setLEDs(leds);
		} else if (reader instanceof Acr1283LReader) {
			return ((Acr1283LReader) reader).setLEDs(leds);
		} else if (reader instanceof Acr1552UReader) {
			return ((Acr1552UReader) reader).setLEDs(leds);
		}
		throw new IllegalArgumentException("Not supported for " + reader.getName());
	}

	// ACR1222L / ACR1283L status LEDs

	private void addLightLEDSection(AcrReader reader) {
		container.addView(section(getString(R.string.acr_controls_light_led)));
		CheckBox ready = checkBox("ready");
		CheckBox progress = checkBox("progress");
		CheckBox complete = checkBox("complete");
		CheckBox error = checkBox("error");
		container.addView(row(ready, progress, complete, error));
		container.addView(row(button(getString(R.string.acr_controls_apply), (v) -> {
			boolean r = ready.isChecked();
			boolean p = progress.isChecked();
			boolean c = complete.isChecked();
			boolean e = error.isChecked();
			run("lightLED(" + r + ", " + p + ", " + c + ", " + e + ")", () -> {
				if (reader instanceof Acr1222LReader) {
					return ((Acr1222LReader) reader).lightLED(r, p, c, e);
				}
				return ((Acr1283LReader) reader).lightLED(r, p, c, e);
			});
		})));
	}

	// display

	private void addDisplaySection(AcrReader reader) {
		container.addView(section(getString(R.string.acr_controls_display)));

		RadioGroup fonts = new RadioGroup(this);
		fonts.setOrientation(RadioGroup.HORIZONTAL);
		for (AcrFont font : AcrFont.values()) {
			RadioButton button = new RadioButton(this);
			button.setId(View.generateViewId());
			button.setText(font.name());
			button.setTag(font);
			button.setChecked(font == AcrFont.FONT_C);
			fonts.addView(button);
		}
		CheckBox bold = checkBox("bold");
		container.addView(row(fonts, bold));

		EditText line = editText("0", InputType.TYPE_CLASS_NUMBER);
		EditText message = editText("Hello ÆØÅ æøå", InputType.TYPE_CLASS_TEXT);
		container.addView(row(text(getString(R.string.acr_controls_line), false), line));
		container.addView(message);

		container.addView(row(button(getString(R.string.acr_controls_show), (v) -> {
			AcrFont font = (AcrFont) findViewById(fonts.getCheckedRadioButtonId()).getTag();
			int style = bold.isChecked() ? Typeface.BOLD : Typeface.NORMAL;
			int lineNumber = parseInt(line, 0);
			String text = message.getText().toString();
			run("displayText(" + font + ", " + (bold.isChecked() ? "bold" : "normal") + ", line " + lineNumber + ", \"" + text + "\")", () -> {
				if (reader instanceof Acr1222LReader) {
					return ((Acr1222LReader) reader).displayText(font, style, lineNumber, 0, text);
				}
				return ((Acr1283LReader) reader).displayText(font, style, lineNumber, 0, text);
			});
		}), button(getString(R.string.acr_controls_clear), (v) -> run("clearDisplay()", () -> {
			if (reader instanceof Acr1222LReader) {
				return ((Acr1222LReader) reader).clearDisplay();
			}
			return ((Acr1283LReader) reader).clearDisplay();
		}))));

		container.addView(row(button(getString(R.string.acr_controls_backlight_on), (v) -> run("lightDisplayBacklight(true)", () -> backlight(reader, true))),
				button(getString(R.string.acr_controls_backlight_off), (v) -> run("lightDisplayBacklight(false)", () -> backlight(reader, false)))));

		if (reader instanceof Acr1283LReader) {
			EditText contrast = editText("8", InputType.TYPE_CLASS_NUMBER);
			container.addView(row(text(getString(R.string.acr_controls_contrast), false), contrast, button(getString(R.string.acr_controls_apply), (v) -> {
				int value = parseInt(contrast, 8);
				run("setDisplayContrast(" + value + ")", () -> ((Acr1283LReader) reader).setDisplayContrast(value));
			})));
		}
	}

	private boolean backlight(AcrReader reader, boolean on) {
		if (reader instanceof Acr1222LReader) {
			return ((Acr1222LReader) reader).lightDisplayBacklight(on);
		}
		return ((Acr1283LReader) reader).lightDisplayBacklight(on);
	}

	// buzzer

	private void addBuzzerSection(AcrReader reader) {
		if (reader instanceof Acr122UReader) {
			Acr122UReader acr122UReader = (Acr122UReader) reader;
			container.addView(section(getString(R.string.acr_controls_buzzer)));
			container.addView(row(
					button(getString(R.string.acr_controls_card_detection_on), (v) -> run("setBuzzerForCardDetection(true)", () -> acr122UReader.setBuzzerForCardDetection(true))),
					button(getString(R.string.acr_controls_card_detection_off), (v) -> run("setBuzzerForCardDetection(false)", () -> acr122UReader.setBuzzerForCardDetection(false)))));
		} else if (reader instanceof Acr1252UReader) {
			Acr1252UReader acr1252UReader = (Acr1252UReader) reader;
			container.addView(section(getString(R.string.acr_controls_buzzer)));
			container.addView(row(button(getString(R.string.acr_controls_on), (v) -> run("setBuzzer(true)", () -> acr1252UReader.setBuzzer(true))),
					button(getString(R.string.acr_controls_off), (v) -> run("setBuzzer(false)", () -> acr1252UReader.setBuzzer(false)))));
		} else if (reader instanceof Acr1552UReader) {
			Acr1552UReader acr1552UReader = (Acr1552UReader) reader;
			container.addView(section(getString(R.string.acr_controls_buzzer)));
			EditText duration = editText("200", InputType.TYPE_CLASS_NUMBER);
			container.addView(row(text(getString(R.string.acr_controls_duration), false), duration, button(getString(R.string.acr_controls_beep), (v) -> {
				int millis = parseInt(duration, 200);
				run("setBuzzerControlSingle(" + millis + ")", () -> acr1552UReader.setBuzzerControlSingle(millis));
			}), button(getString(R.string.acr_controls_beep_3), (v) -> run("setBuzzerControlRepeat(100, 100, 3)", () -> acr1552UReader.setBuzzerControlRepeat(100, 100, 3)))));
		}
	}

	// helpers

	private void run(String description, Callable<Object> call) {
		log(description + " ...");
		executor.execute(() -> {
			try {
				Object result = call.call();
				log(description + " -> " + result);
			} catch (Exception e) {
				Log.w(LOG_TAG, description, e);
				log(description + " failed: " + e);
			}
		});
	}

	private void log(String line) {
		Log.i(LOG_TAG, line);
		String text = timeFormat.format(new Date()) + " " + line;
		runOnUiThread(() -> {
			logLines.add(0, text);
			while (logLines.size() > 50) {
				logLines.remove(logLines.size() - 1);
			}
			StringBuilder builder = new StringBuilder();
			for (String l : logLines) {
				builder.append(l).append('\n');
			}
			log.setText(builder);
		});
	}

	private static <T extends Enum<T>> List<T> checked(Map<T, CheckBox> checkBoxes) {
		List<T> checked = new ArrayList<>();
		for (Map.Entry<T, CheckBox> entry : checkBoxes.entrySet()) {
			if (entry.getValue().isChecked()) {
				checked.add(entry.getKey());
			}
		}
		return checked;
	}

	private static int parseInt(EditText editText, int defaultValue) {
		try {
			return Integer.parseInt(editText.getText().toString().trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	private TextView section(String title) {
		TextView view = text(title, true);
		view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		view.setPadding(0, dp(16), 0, dp(4));
		return view;
	}

	private TextView text(String value, boolean bold) {
		TextView view = new TextView(this);
		view.setText(value);
		view.setTextColor(Color.BLACK);
		if (bold) {
			view.setTypeface(Typeface.DEFAULT_BOLD);
		}
		return view;
	}

	private CheckBox checkBox(String label) {
		CheckBox checkBox = new CheckBox(this);
		checkBox.setText(label);
		return checkBox;
	}

	private EditText editText(String value, int inputType) {
		EditText editText = new EditText(this);
		editText.setText(value);
		editText.setInputType(inputType);
		editText.setMinWidth(dp(64));
		return editText;
	}

	private Button button(String label, View.OnClickListener listener) {
		Button button = new Button(this);
		button.setText(label);
		button.setOnClickListener(listener);
		return button;
	}

	private LinearLayout row(View... views) {
		LinearLayout row = new LinearLayout(this);
		row.setOrientation(LinearLayout.HORIZONTAL);
		for (View view : views) {
			row.addView(view);
		}
		return row;
	}

	private int dp(int value) {
		return Math.round(value * getResources().getDisplayMetrics().density);
	}
}
