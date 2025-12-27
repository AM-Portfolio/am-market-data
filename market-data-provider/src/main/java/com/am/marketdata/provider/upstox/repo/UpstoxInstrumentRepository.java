package com.am.marketdata.provider.upstox.repo;

import com.am.marketdata.provider.upstox.model.UpstoxInstrument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UpstoxInstrumentRepository extends MongoRepository<UpstoxInstrument, String> {

    List<UpstoxInstrument> findByAssetSymbolIn(List<String> assetSymbols);

    List<UpstoxInstrument> findByTradingSymbolIn(List<String> tradingSymbols);

    @Query("{ '$or': [ { 'name': { '$regex': ?0, '$options': 'i' } }, { 'assetSymbol': { '$regex': ?0, '$options': 'i' } }, { 'tradingSymbol': { '$regex': ?0, '$options': 'i' } } ] }")
    List<UpstoxInstrument> searchByText(String searchText);

    List<UpstoxInstrument> findByAssetSymbolAndInstrumentType(String assetSymbol, String instrumentType);
}
