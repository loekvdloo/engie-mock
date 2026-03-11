package com.engie.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "berichten")
public class BerichtEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "message_id")
    private String messageId;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "sender_ean", length = 18)
    private String senderEan;

    @Column(name = "receiver_ean", length = 18)
    private String receiverEan;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "xml_payload", columnDefinition = "LONGTEXT", nullable = false)
    private String xmlPayload;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "ONTVANGEN";

    @Column(name = "ontvangst_tijd", nullable = false)
    private LocalDateTime ontvangstTijd;

    @Column(name = "aangemaakt_op", nullable = false, updatable = false)
    private LocalDateTime aangemaaktOp;

    @OneToMany(mappedBy = "bericht", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ValidatieFoutEntity> validatieFouten = new ArrayList<>();

    @OneToMany(mappedBy = "bericht", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BerichtLogEntity> logs = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (aangemaaktOp == null) aangemaaktOp = LocalDateTime.now();
        if (ontvangstTijd == null) ontvangstTijd = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getSenderEan() { return senderEan; }
    public void setSenderEan(String senderEan) { this.senderEan = senderEan; }

    public String getReceiverEan() { return receiverEan; }
    public void setReceiverEan(String receiverEan) { this.receiverEan = receiverEan; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getXmlPayload() { return xmlPayload; }
    public void setXmlPayload(String xmlPayload) { this.xmlPayload = xmlPayload; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getOntvangstTijd() { return ontvangstTijd; }
    public void setOntvangstTijd(LocalDateTime ontvangstTijd) { this.ontvangstTijd = ontvangstTijd; }

    public LocalDateTime getAangemaaktOp() { return aangemaaktOp; }

    public List<ValidatieFoutEntity> getValidatieFouten() { return validatieFouten; }
    public List<BerichtLogEntity> getLogs() { return logs; }
}
