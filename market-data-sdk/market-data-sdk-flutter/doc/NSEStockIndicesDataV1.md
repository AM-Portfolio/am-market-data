# market_data_client.model.NSEStockIndicesDataV1

## Load the model package
```dart
import 'package:market_data_client/api.dart';
```

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **String** |  | [optional] 
**advance** | [**Advance**](Advance.md) |  | [optional] 
**timestamp** | **String** |  | [optional] 
**data** | [**List<StockData>**](StockData.md) |  | [optional] [default to const []]
**metadata** | [**IndexMetadata**](IndexMetadata.md) |  | [optional] 
**marketStatus** | [**MarketStatus**](MarketStatus.md) |  | [optional] 
**date30dAgo** | **String** |  | [optional] 
**date365dAgo** | **String** |  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


