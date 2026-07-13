package org.eclipse.cargotrakcer.regapp.client;

public class HandlingReport {

    //@NotNull
    //@Size(min = 16, max = 16)
    private String completionTime;

    //@NotNull
    //@Size(min = 4)
    private String trackingId;

    //@NotNull
    //@Size(min = 4, max = 7)
    private String eventType;

    //@NotNull
    //@Size(min = 5, max = 5)
    private String unLocode;

    //@Size(min = 4, max = 5)
    private String voyageNumber;

    public String getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(String value) {
        this.completionTime = value;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String value) {
        this.eventType = value;
    }

    public String getUnLocode() {
        return unLocode;
    }

    public void setUnLocode(String value) {
        this.unLocode = value;
    }

    public String getVoyageNumber() {
        return voyageNumber;
    }

    public void setVoyageNumber(String value) {
        this.voyageNumber = value;
    }

    @Override
    public String toString() {
        return "HandlingReport{" +
                "completionTime='" + completionTime + '\'' +
                ", trackingId='" + trackingId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", unLocode='" + unLocode + '\'' +
                ", voyageNumber='" + voyageNumber + '\'' +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String completionTime;

        private String trackingId;

        private String eventType;

        private String unLocode;

        private String voyageNumber;

        public Builder completionTime(String completionTime) {
            this.completionTime = completionTime;
            return this;
        }

        public Builder trackingId(String trackingId) {
            this.trackingId = trackingId;
            return this;
        }

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder unLocode(String unLocode) {
            this.unLocode = unLocode;
            return this;
        }

        public Builder voyageNumber(String voyageNumber) {
            this.voyageNumber = voyageNumber;
            return this;
        }

        public HandlingReport build() {
            var report = new HandlingReport();
            // TODO validating the data
            report.setCompletionTime(this.completionTime);
            report.setEventType(this.eventType);
            report.setTrackingId(this.trackingId);
            report.setUnLocode(this.unLocode);
            report.setVoyageNumber(this.voyageNumber);

            return report;
        }
    }
}

