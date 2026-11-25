package no.entur.abt.nfc.example.ultralight;

public class Outcome {

	public enum Status {
		/** the command returned the expected kind of response */
		OK,
		/** the command failed (exception, NAK, unexpected response) */
		FAILED,
		/** the tag was lost, i.e. removed too early */
		LOST,
		/** the command was not run */
		SKIPPED
	}

	public static Outcome ok(String detail) {
		return new Outcome(Status.OK, detail);
	}

	public static Outcome failed(String detail) {
		return new Outcome(Status.FAILED, detail);
	}

	public static Outcome lost(String detail) {
		return new Outcome(Status.LOST, detail);
	}

	public static Outcome skipped(String detail) {
		return new Outcome(Status.SKIPPED, detail);
	}

	private final Status status;
	private final String detail;

	public Outcome(Status status, String detail) {
		this.status = status;
		this.detail = detail;
	}

	public Status getStatus() {
		return status;
	}

	public String getDetail() {
		return detail;
	}

	public boolean isOk() {
		return status == Status.OK;
	}

	public boolean isRun() {
		return status == Status.OK || status == Status.FAILED;
	}

	@Override
	public String toString() {
		return status + (detail != null ? " " + detail : "");
	}
}
