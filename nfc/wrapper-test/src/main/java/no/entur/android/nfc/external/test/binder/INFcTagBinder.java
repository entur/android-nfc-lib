package no.entur.android.nfc.external.test.binder;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.RemoteException;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import no.entur.android.nfc.external.test.tech.MockBasicTagTechnology;
import no.entur.android.nfc.external.test.tech.MockIsoDep;
import no.entur.android.nfc.external.test.tech.MockMifareClassic;
import no.entur.android.nfc.external.test.tech.MockMifareUltralight;
import no.entur.android.nfc.external.test.tech.MockNdef;
import no.entur.android.nfc.external.test.tech.MockNdefFormatable;
import no.entur.android.nfc.external.test.tech.MockNfcA;
import no.entur.android.nfc.external.test.tech.MockNfcB;
import no.entur.android.nfc.external.test.tech.MockNfcF;
import no.entur.android.nfc.external.test.tech.MockNfcV;
import no.entur.android.nfc.wrapper.ErrorCodes;
import no.entur.android.nfc.wrapper.TagImpl;
import no.entur.android.nfc.wrapper.TransceiveResult;
import no.entur.android.nfc.wrapper.tech.BasicTagTechnology;
import no.entur.android.nfc.wrapper.tech.Ndef;
import no.entur.android.nfc.wrapper.tech.TagTechnology;

public class INFcTagBinder {

    private static final String LOG_TAG = INFcTagBinder.class.getName();

    private final int[] techList;

    private MockNdef ndef;
    private MockNdefFormatable ndefFormatable;
    private MockNfcA nfcA;
    private MockNfcB nfcB;
    private MockNfcF nfcF;
    private MockNfcV nfcV;

    private MockIsoDep isoDep;
    private MockMifareClassic mifareClassic;
    private MockMifareUltralight mifareUltralight;

    private int connected = -1;

    private final int maxTransceiveLength;
    private final boolean extendedLengthApdusSupported;
    private final int defaultTimeout;
    private int timeout;

    private final TagImpl tagImpl;

    public INFcTagBinder(List<MockBasicTagTechnology> technologies, int maxTransceiveLength, boolean extendedLengthApdusSupported, int defaultTimeout, TagImpl tagImpl) {
        this.maxTransceiveLength = maxTransceiveLength;
        this.extendedLengthApdusSupported = extendedLengthApdusSupported;
        this.defaultTimeout = defaultTimeout;
        this.timeout = defaultTimeout;
        this.tagImpl = tagImpl;

        int[] ids = new int[technologies.size()];
        for(int i = 0; i < technologies.size(); i++) {
            MockBasicTagTechnology b = technologies.get(i);
            if(b instanceof MockNdef) {
                ndef = (MockNdef) b;
                ids[i] = BasicTagTechnology.NDEF;
            } else if(b instanceof MockNdefFormatable) {
                ndefFormatable = (MockNdefFormatable) b;
                ids[i] = BasicTagTechnology.NDEF;
            } else if(b instanceof MockNfcA) {
                nfcA = (MockNfcA) b;
                ids[i] = BasicTagTechnology.NFC_A;
            } else if(b instanceof MockNfcB) {
                nfcB = (MockNfcB) b;
                ids[i] = BasicTagTechnology.NFC_B;
            } else if(b instanceof MockNfcF) {
                nfcF = (MockNfcF) b;
                ids[i] = BasicTagTechnology.NFC_F;
            } else if(b instanceof MockNfcV) {
                nfcV = (MockNfcV) b;
                ids[i] = BasicTagTechnology.NFC_V;
            } else if(b instanceof MockIsoDep) {
                isoDep = (MockIsoDep) b;
                ids[i] = BasicTagTechnology.ISO_DEP;
            } else if(b instanceof MockMifareClassic) {
                mifareClassic = (MockMifareClassic) b;
                ids[i] = BasicTagTechnology.MIFARE_CLASSIC;
            } else if(b instanceof MockMifareUltralight) {
                mifareUltralight = (MockMifareUltralight) b;
                ids[i] = BasicTagTechnology.MIFARE_ULTRALIGHT;
            }
        }

        this.techList = ids;
    }

	public boolean canMakeReadOnly(int ndefType) throws RemoteException {
        if(ndef == null) {
            throw new RemoteException();
        }
		return ndef.canMakeReadOnly();
	}

	public int connect(int technology) throws RemoteException {
        MockBasicTagTechnology target = null;
        
        switch (technology) {
            case BasicTagTechnology.ISO_DEP: {
                target = isoDep;
                break;
            }
            case BasicTagTechnology.MIFARE_CLASSIC: {
                target = mifareClassic;
                break;
            }
            case BasicTagTechnology.MIFARE_ULTRALIGHT: {
                target = mifareUltralight;
                break;
            }
            case BasicTagTechnology.NDEF: {
                target = ndef;
                break;
            }
            case BasicTagTechnology.NDEF_FORMATABLE: {
                target = ndefFormatable;
                break;
            }
            case BasicTagTechnology.NFC_A: {
                target = nfcA;
                break;
            }
            case BasicTagTechnology.NFC_B: {
                target = nfcB;
                break;
            }
            case BasicTagTechnology.NFC_F: {
                target = nfcF;
                break;
            }
            case BasicTagTechnology.NFC_V: {
                target = nfcV;
                break;
            }
            default : {
                // do nothing
            }
        }

        if(target == null) {
            return ErrorCodes.ERROR_CONNECT;
        }

        if(connected != -1 && connected != technology) {
            return ErrorCodes.ERROR_CONNECT;
        }
        
        this.connected = technology;
        return ErrorCodes.SUCCESS;
	}

    public int reconnect() throws RemoteException {
        return ErrorCodes.SUCCESS;
    }

    protected void checkConnected(int technology) throws RemoteException {
        if(technology == -1) {
            throw new RemoteException("No connected tag technology");
        }
    }

	public int formatNdef(byte[] key) throws RemoteException {
        checkConnected(TagTechnology.NDEF_FORMATABLE);

        try {
            ndefFormatable.format(new NdefMessage(new NdefRecord[0]));

            return ErrorCodes.SUCCESS;
        } catch (Exception e) {
            // TODO is this the right code?
            return ErrorCodes.ERROR_WRITE;
        }
	}

	public boolean getExtendedLengthApdusSupported() throws RemoteException {
        return extendedLengthApdusSupported;
	}

    public int getMaxTransceiveLength(int technology) throws RemoteException {
        return maxTransceiveLength;
	}

	public int[] getTechList() throws RemoteException {
		return techList;
	}

	public int getTimeout(int technology) throws RemoteException {
        return timeout;
	}

    public void resetTimeouts() throws RemoteException {
        this.timeout = defaultTimeout;
    }

    public boolean isNdef() throws RemoteException {
        return ndef != null;
    }

	public boolean ndefIsWritable() throws RemoteException {
        return ndef != null && ndef.isWritable();
    }

	public int ndefMakeReadOnly() throws RemoteException {
        checkConnected(Ndef.NDEF);

        try {
            if(ndef.makeReadOnly()) {
                return ErrorCodes.SUCCESS;
            }
            return ErrorCodes.ERROR_IO;
        } catch (Exception e) {
            return ErrorCodes.ERROR_IO;
        }
	}

	public NdefMessage ndefRead() throws RemoteException {
        checkConnected(Ndef.NDEF);

        try {
            return ndef.getNdefMessage();
        } catch (Exception e) {
            throw new RemoteException("Problem reading NDEF: " + e.getMessage());
        }
	}

	public int ndefWrite(NdefMessage msg) throws RemoteException {
        checkConnected(Ndef.NDEF);

        try {
            ndef.writeNdefMessage(msg);

            return ErrorCodes.SUCCESS;
        } catch (Exception e) {
            return ErrorCodes.ERROR_IO;
        }
	}

	public TagImpl rediscover() throws RemoteException {
        return tagImpl;
	}

	public int setTimeout(int technology, int timeout) throws RemoteException {
        this.timeout = timeout;
        return ErrorCodes.SUCCESS;
	}

	public TransceiveResult transceive(byte[] data, boolean raw) throws RemoteException {
        if(connected == -1) {
            throw new RemoteException("No connected tag technology");
        }

        try {
            if(connected == TagTechnology.ISO_DEP) {
                byte[] transceive = isoDep.transceive(data);

                return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, transceive);
            } else if(connected == TagTechnology.MIFARE_ULTRALIGHT) {
                byte[] transceive = mifareUltralight.transceive(data);

                return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, transceive);
            } else if(connected == TagTechnology.MIFARE_CLASSIC) {
                byte[] transceive = mifareClassic.transceive(data);

                return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, transceive);
            } else if(connected == TagTechnology.NFC_A) {
                byte[] transceive = nfcA.transceive(data);

                return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, transceive);
            } else if(connected == TagTechnology.NFC_B) {
                byte[] transceive = nfcB.transceive(data);

                return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, transceive);
            } else if(connected == TagTechnology.NFC_F) {
                byte[] transceive = nfcF.transceive(data);

                return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, transceive);
            } else if(connected == TagTechnology.NFC_V) {
                byte[] transceive = nfcV.transceive(data);

                return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, transceive);
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Transceive problem", e);
        }

        return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
	}


}
