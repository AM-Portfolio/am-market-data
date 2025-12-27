//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.18

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;


class MarketDataPollingApi {
  MarketDataPollingApi([ApiClient? apiClient]) : apiClient = apiClient ?? defaultApiClient;

  final ApiClient apiClient;

  /// Get connection status
  ///
  /// Returns streaming connection status
  ///
  /// Note: This method returns the HTTP [Response].
  Future<Response> getConnectionStatusWithHttpInfo() async {
    // ignore: prefer_const_declarations
    final path = r'/api/v1/polling/status';

    // ignore: prefer_final_locals
    Object? postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    const contentTypes = <String>[];


    return apiClient.invokeAPI(
      path,
      'GET',
      queryParams,
      postBody,
      headerParams,
      formParams,
      contentTypes.isEmpty ? null : contentTypes.first,
    );
  }

  /// Get connection status
  ///
  /// Returns streaming connection status
  Future<Map<String, Object>?> getConnectionStatus() async {
    final response = await getConnectionStatusWithHttpInfo();
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      return Map<String, Object>.from(await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'Map<String, Object>'),);

    }
    return null;
  }

  /// Internal use for SDK generation
  ///
  /// Note: This method returns the HTTP [Response].
  Future<Response> getUpdateSchemaWithHttpInfo() async {
    // ignore: prefer_const_declarations
    final path = r'/api/v1/polling/schema/update';

    // ignore: prefer_final_locals
    Object? postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    const contentTypes = <String>[];


    return apiClient.invokeAPI(
      path,
      'GET',
      queryParams,
      postBody,
      headerParams,
      formParams,
      contentTypes.isEmpty ? null : contentTypes.first,
    );
  }

  /// Internal use for SDK generation
  Future<MarketDataUpdateV1?> getUpdateSchema() async {
    final response = await getUpdateSchemaWithHttpInfo();
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'MarketDataUpdateV1',) as MarketDataUpdateV1;
    
    }
    return null;
  }

  /// Poll market data
  ///
  /// Poll live market data for given symbols
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [String] symbols (required):
  ///
  /// * [String] timeFrame:
  ///
  /// * [bool] indexSymbol:
  Future<Response> pollMarketDataWithHttpInfo(String symbols, { String? timeFrame, bool? indexSymbol, }) async {
    // ignore: prefer_const_declarations
    final path = r'/api/v1/polling/data';

    // ignore: prefer_final_locals
    Object? postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

      queryParams.addAll(_queryParams('', 'symbols', symbols));
    if (timeFrame != null) {
      queryParams.addAll(_queryParams('', 'timeFrame', timeFrame));
    }
    if (indexSymbol != null) {
      queryParams.addAll(_queryParams('', 'indexSymbol', indexSymbol));
    }

    const contentTypes = <String>[];


    return apiClient.invokeAPI(
      path,
      'GET',
      queryParams,
      postBody,
      headerParams,
      formParams,
      contentTypes.isEmpty ? null : contentTypes.first,
    );
  }

  /// Poll market data
  ///
  /// Poll live market data for given symbols
  ///
  /// Parameters:
  ///
  /// * [String] symbols (required):
  ///
  /// * [String] timeFrame:
  ///
  /// * [bool] indexSymbol:
  Future<Map<String, Object>?> pollMarketData(String symbols, { String? timeFrame, bool? indexSymbol, }) async {
    final response = await pollMarketDataWithHttpInfo(symbols,  timeFrame: timeFrame, indexSymbol: indexSymbol, );
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      return Map<String, Object>.from(await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'Map<String, Object>'),);

    }
    return null;
  }

  /// Subscribe to symbols
  ///
  /// Subscribe to symbols for real-time updates
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [List<String>] requestBody (required):
  Future<Response> subscribeWithHttpInfo(List<String> requestBody,) async {
    // ignore: prefer_const_declarations
    final path = r'/api/v1/polling/subscribe';

    // ignore: prefer_final_locals
    Object? postBody = requestBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    const contentTypes = <String>['application/json'];


    return apiClient.invokeAPI(
      path,
      'POST',
      queryParams,
      postBody,
      headerParams,
      formParams,
      contentTypes.isEmpty ? null : contentTypes.first,
    );
  }

  /// Subscribe to symbols
  ///
  /// Subscribe to symbols for real-time updates
  ///
  /// Parameters:
  ///
  /// * [List<String>] requestBody (required):
  Future<Map<String, Object>?> subscribe(List<String> requestBody,) async {
    final response = await subscribeWithHttpInfo(requestBody,);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      return Map<String, Object>.from(await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'Map<String, Object>'),);

    }
    return null;
  }

  /// Unsubscribe from symbols
  ///
  /// Unsubscribe from symbols
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [List<String>] requestBody (required):
  Future<Response> unsubscribeWithHttpInfo(List<String> requestBody,) async {
    // ignore: prefer_const_declarations
    final path = r'/api/v1/polling/unsubscribe';

    // ignore: prefer_final_locals
    Object? postBody = requestBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    const contentTypes = <String>['application/json'];


    return apiClient.invokeAPI(
      path,
      'POST',
      queryParams,
      postBody,
      headerParams,
      formParams,
      contentTypes.isEmpty ? null : contentTypes.first,
    );
  }

  /// Unsubscribe from symbols
  ///
  /// Unsubscribe from symbols
  ///
  /// Parameters:
  ///
  /// * [List<String>] requestBody (required):
  Future<Map<String, Object>?> unsubscribe(List<String> requestBody,) async {
    final response = await unsubscribeWithHttpInfo(requestBody,);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      return Map<String, Object>.from(await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'Map<String, Object>'),);

    }
    return null;
  }
}
