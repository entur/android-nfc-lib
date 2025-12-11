package no.entur.android.nfc.external.hwb.card;

import android.content.Context;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import hwb.utilities.mqtt3.client.MqttServiceClient;
import hwb.utilities.validators.nfc.CardContent;
import hwb.utilities.validators.nfc.apdu.receive.ReceiveSchema;
import no.entur.android.nfc.external.hwb.HwbMqttClient;
import no.entur.android.nfc.external.hwb.intent.ExternalHfcAtr210TagCallback;
import no.entur.android.nfc.external.hwb.intent.Atr210TransceiveResultExceptionMapper;
import no.entur.android.nfc.external.service.tag.INFcTagBinder;
import no.entur.android.nfc.external.service.tag.TagProxy;
import no.entur.android.nfc.external.service.tag.TagProxyStore;
import no.entur.android.nfc.external.tag.IsoDepTagServiceSupport;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestMessageListener;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestMessageRequest;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessageListener;

public class Atr210CardService implements SynchronizedRequestMessageListener<UUID> {

    private static final Atr210CardMessageConverter CONVERTER = new Atr210CardMessageConverter();

    protected final SynchronizedRequestResponseMessages<UUID> adpuRequestResponseMessages;
    protected final IsoDepTagServiceSupport isoDepTagServiceSupport;

    protected Atr210CardContext cardContext;

    protected Atr210CardCommands commands;

    protected long transcieveTimeout;

    protected MqttServiceClient hwbMqttClient;

    protected INFcTagBinder infcTagBinder;
    protected TagProxyStore tagProxyStore;

    protected TagProxy currentCard;

    public Atr210CardService(Context context, SynchronizedRequestResponseMessages<UUID> adpuRequestResponseMessages, MqttServiceClient hwbMqttClient, long transcieveTimeout, INFcTagBinder infcTagBinder, TagProxyStore tagProxyStore) {
        this.adpuRequestResponseMessages = adpuRequestResponseMessages;
        this.hwbMqttClient = hwbMqttClient;
        this.transcieveTimeout = transcieveTimeout;

        this.infcTagBinder = infcTagBinder;
        this.tagProxyStore = tagProxyStore;

        this.isoDepTagServiceSupport = new IsoDepTagServiceSupport(context, infcTagBinder, tagProxyStore, new Atr210TransceiveResultExceptionMapper());
    }

    public void setCardContext(Atr210CardContext cardContext) {
        this.cardContext = cardContext;

        this.commands = Atr210CardCommands.newBuilder()
                .withCardContext(cardContext)
                .withCardMessageConverter(CONVERTER)
                .withAdpuExchange(adpuRequestResponseMessages)
                .withAdpuTranscieveTimeout(transcieveTimeout)
                .build();
    }

    public void onAdpuResponse(ReceiveSchema receiveSchema) {
        adpuRequestResponseMessages.onResponseMessage(new Atr210CardAdpuSynchronizedResponseMessage(receiveSchema));
    }

    @Override
    public void onRequestMessage(SynchronizedRequestMessageRequest<UUID> message, SynchronizedResponseMessageListener<UUID> listener) throws IOException {
        hwbMqttClient.publish(message.getTopic(), message.getPayload());
    }

    public void createTag(String travelCardNumber, String token, List<CardContent> cardContent) {
        Atr210IsoDepWrapper wrapper = new Atr210IsoDepWrapper(commands);

        TagProxy currentCard = this.currentCard;
        if(currentCard != null) {
            try {
                currentCard.close();
            } catch (Exception e) {
                // ignore
            } finally {
                this.currentCard = null;
            }
        }

        // broadcast tag present
        this.currentCard = isoDepTagServiceSupport.card(0, wrapper, cardContext.getUid(), cardContext.getHistoricalBytes(), (intent) -> {

            intent.putExtra(ExternalHfcAtr210TagCallback.EXTRA_HWB_DEVICE_ID, cardContext.getDeviceId());
            if (token != null) {
                intent.putExtra(ExternalHfcAtr210TagCallback.EXTRA_HWB_TOKEN, token);
            }
            if (travelCardNumber != null) {
                intent.putExtra(ExternalHfcAtr210TagCallback.EXTRA_HWB_TRAVEL_CARD_NUMBER, travelCardNumber);
            }
            if (cardContent != null) {
                intent.putExtra(ExternalHfcAtr210TagCallback.EXTRA_HWB_CARD_CONTENT, cardContent.toArray(new CardContent[cardContent.size()]));
            }

            intent.putExtra(ExternalHfcAtr210TagCallback.EXTRA_TRACE_ID, cardContext.getTraceId().toString());

            return intent;
        });
    }

    public void clearCardContext() {
        Atr210CardContext cardContext = this.cardContext;
        if(cardContext != null) {
            cardContext.setClosed(true);

            this.cardContext = null;
        }
    }

    public void close() {
        clearCardContext();
    }

}
