package no.entur.android.nfc.external.hid.card;

import android.content.Context;

import org.nfctools.api.ATR;
import org.nfctools.api.TagType;
import org.nfctools.api.detect.DefaultTagTypeDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hwb.utilities.mqtt3.client.MqttServiceClient;
import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.external.hid.dto.atr210.NfcAdpuTransmitResponse;
import no.entur.android.nfc.external.service.tag.INFcTagBinder;
import no.entur.android.nfc.external.service.tag.TagProxy;
import no.entur.android.nfc.external.service.tag.TagProxyStore;
import no.entur.android.nfc.external.tag.IsoDepTagServiceSupport;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;

public class Atr210CardService {

    private static final Logger LOGGER = LoggerFactory.getLogger(Atr210CardService.class);

    private static final Atr210CardMessageConverter CONVERTER = new Atr210CardMessageConverter();

    protected final SynchronizedRequestResponseMessages<String> adpuRequestResponseMessages;
    protected final IsoDepTagServiceSupport isoDepTagServiceSupport;

    protected Atr210CardContext cardContext;

    protected Atr210CardCommands commands;

    protected long transcieveTimeout;

    protected MqttServiceClient mqttClient;

    protected INFcTagBinder infcTagBinder;
    protected TagProxyStore tagProxyStore;

    protected TagProxy currentCard;

    public Atr210CardService(Context context, SynchronizedRequestResponseMessages<String> adpuRequestResponseMessages, MqttServiceClient mqttClient, long transcieveTimeout, INFcTagBinder infcTagBinder, TagProxyStore tagProxyStore) {
        this.adpuRequestResponseMessages = adpuRequestResponseMessages;
        this.mqttClient = mqttClient;
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

    public void onAdpuResponse(String topic, NfcAdpuTransmitResponse receiveSchema) {
        adpuRequestResponseMessages.onResponseMessage(new Atr210CardAdpuSynchronizedResponseMessage(topic, receiveSchema));
    }

    public void createTag() {
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

        DefaultTagTypeDetector detector = new DefaultTagTypeDetector();
        TagType tagType = detector.parseHistoricalBytes(null, cardContext.getAtr());

        if(tagType == TagType.ISO_DEP || tagType == TagType.DESFIRE_EV1 || tagType == TagType.ISO_14443_TYPE_A) {

            // broadcast tag present
            this.currentCard = isoDepTagServiceSupport.card(0, wrapper, cardContext.getUid(), cardContext.getHistoricalBytes(), (intent) -> {

                intent.putExtra(Atr210NfcTagCallback.EXTRA_PROVIDER_ID, cardContext.getProviderId());
                intent.putExtra(Atr210NfcTagCallback.EXTRA_CLIENT_ID, cardContext.getClientId());

                return intent;
            });
        } else {
            if(LOGGER.isDebugEnabled()) LOGGER.debug("Broadcast tech discovered");
            // broadcast tag tech
            isoDepTagServiceSupport.broadcast(ExternalNfcTagCallback.ACTION_TECH_DISCOVERED);
        }
    }

    public void onTagLost() {
        clearCardContext();

        // TODO tag lost

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
