package com.asyncapi.models;

public class MarketDataUpdate {
  private String instrumentKey;
  private Double lastPrice;
  private Double change;
  private Double open;
  private Double high;
  private Double low;
  private Double previousClose;
  private Long volume;
  private String timestamp;
  private Double pChange;
  private Map<String, Object> additionalProperties;

  public String getInstrumentKey() { return this.instrumentKey; }
  public void setInstrumentKey(String instrumentKey) { this.instrumentKey = instrumentKey; }

  public Double getLastPrice() { return this.lastPrice; }
  public void setLastPrice(Double lastPrice) { this.lastPrice = lastPrice; }

  public Double getChange() { return this.change; }
  public void setChange(Double change) { this.change = change; }

  public Double getOpen() { return this.open; }
  public void setOpen(Double open) { this.open = open; }

  public Double getHigh() { return this.high; }
  public void setHigh(Double high) { this.high = high; }

  public Double getLow() { return this.low; }
  public void setLow(Double low) { this.low = low; }

  public Double getPreviousClose() { return this.previousClose; }
  public void setPreviousClose(Double previousClose) { this.previousClose = previousClose; }

  public Long getVolume() { return this.volume; }
  public void setVolume(Long volume) { this.volume = volume; }

  public String getTimestamp() { return this.timestamp; }
  public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

  public Double getPChange() { return this.pChange; }
  public void setPChange(Double pChange) { this.pChange = pChange; }

  public Map<String, Object> getAdditionalProperties() { return this.additionalProperties; }
  public void setAdditionalProperties(Map<String, Object> additionalProperties) { this.additionalProperties = additionalProperties; }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MarketDataUpdate self = (MarketDataUpdate) o;
      return 
        Objects.equals(this.instrumentKey, self.instrumentKey) &&
        Objects.equals(this.lastPrice, self.lastPrice) &&
        Objects.equals(this.change, self.change) &&
        Objects.equals(this.open, self.open) &&
        Objects.equals(this.high, self.high) &&
        Objects.equals(this.low, self.low) &&
        Objects.equals(this.previousClose, self.previousClose) &&
        Objects.equals(this.volume, self.volume) &&
        Objects.equals(this.timestamp, self.timestamp) &&
        Objects.equals(this.pChange, self.pChange) &&
        Objects.equals(this.additionalProperties, self.additionalProperties);
  }

  @Override
  public String toString() {
    return "class MarketDataUpdate {\n" +   
      "    instrumentKey: " + toIndentedString(instrumentKey) + "\n" +
      "    lastPrice: " + toIndentedString(lastPrice) + "\n" +
      "    change: " + toIndentedString(change) + "\n" +
      "    open: " + toIndentedString(open) + "\n" +
      "    high: " + toIndentedString(high) + "\n" +
      "    low: " + toIndentedString(low) + "\n" +
      "    previousClose: " + toIndentedString(previousClose) + "\n" +
      "    volume: " + toIndentedString(volume) + "\n" +
      "    timestamp: " + toIndentedString(timestamp) + "\n" +
      "    pChange: " + toIndentedString(pChange) + "\n" +
      "    additionalProperties: " + toIndentedString(additionalProperties) + "\n" +
    "}";
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}