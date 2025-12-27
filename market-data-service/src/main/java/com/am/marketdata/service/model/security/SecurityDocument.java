package com.am.marketdata.service.model.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "securities")
public class SecurityDocument implements Serializable {

    @Id
    private String id;

    private SecurityKey key;

    private SecurityMetadata metadata;

    private Audit audit;

    @Data
    @Builder
    @AllArgsConstructor
    public static class SecurityKey implements Serializable {
        private String symbol;
        private String isin;

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getIsin() {
            return isin;
        }

        public void setIsin(String isin) {
            this.isin = isin;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityMetadata implements Serializable {
        private String sector;
        private String industry;

        @Field("market_cap_value")
        private Long marketCapValue;

        @Field("market_cap_type")
        private String marketCapType;

        public String getSector() {
            return sector;
        }

        public void setSector(String sector) {
            this.sector = sector;
        }

        public String getIndustry() {
            return industry;
        }

        public void setIndustry(String industry) {
            this.industry = industry;
        }

        public Long getMarketCapValue() {
            return marketCapValue;
        }

        public void setMarketCapValue(Long marketCapValue) {
            this.marketCapValue = marketCapValue;
        }

        public String getMarketCapType() {
            return marketCapType;
        }

        public void setMarketCapType(String marketCapType) {
            this.marketCapType = marketCapType;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Audit implements Serializable {
        @Field("created_at")
        private Instant createdAt;

        private Long version;

        public Instant getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }

        public Long getVersion() {
            return version;
        }

        public void setVersion(Long version) {
            this.version = version;
        }
    }

    public SecurityKey getKey() {
        return key;
    }

    public void setKey(SecurityKey key) {
        this.key = key;
    }
    // metadata and audit already added

    public SecurityMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(SecurityMetadata metadata) {
        this.metadata = metadata;
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }
}
