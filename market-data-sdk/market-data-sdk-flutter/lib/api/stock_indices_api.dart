//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.18

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;


class StockIndicesApi {
  StockIndicesApi([ApiClient? apiClient]) : apiClient = apiClient ?? defaultApiClient;

  final ApiClient apiClient;

  /// Get available indices
  ///
  /// Returns list of available broad market and sector indices
  ///
  /// Note: This method returns the HTTP [Response].
  Future<Response> getAvailableIndicesWithHttpInfo() async {
    // ignore: prefer_const_declarations
    final path = r'/api/v1/stock-indices/available';

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

  /// Get available indices
  ///
  /// Returns list of available broad market and sector indices
  Future<Map<String, List<String>>?> getAvailableIndices() async {
    final response = await getAvailableIndicesWithHttpInfo();
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      return Map<String, List<String>>.from(await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'Map<String, List<String>>'),);

    }
    return null;
  }

  /// Get stock indices data
  ///
  /// Returns constituent stocks for a specific index
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [String] indexSymbol (required):
  ///
  /// * [bool] forceRefresh:
  Future<Response> getStockIndicesWithHttpInfo(String indexSymbol, { bool? forceRefresh, }) async {
    // ignore: prefer_const_declarations
    final path = r'/api/v1/stock-indices/{indexSymbol}'
      .replaceAll('{indexSymbol}', indexSymbol);

    // ignore: prefer_final_locals
    Object? postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    if (forceRefresh != null) {
      queryParams.addAll(_queryParams('', 'forceRefresh', forceRefresh));
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

  /// Get stock indices data
  ///
  /// Returns constituent stocks for a specific index
  ///
  /// Parameters:
  ///
  /// * [String] indexSymbol (required):
  ///
  /// * [bool] forceRefresh:
  Future<NSEStockIndicesDataV1?> getStockIndices(String indexSymbol, { bool? forceRefresh, }) async {
    final response = await getStockIndicesWithHttpInfo(indexSymbol,  forceRefresh: forceRefresh, );
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'NSEStockIndicesDataV1',) as NSEStockIndicesDataV1;
    
    }
    return null;
  }

  /// Get multiple stock indices
  ///
  /// Returns constituent stocks for multiple indices
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [List<String>] requestBody (required):
  ///
  /// * [bool] forceRefresh:
  Future<Response> getStockIndicesBatchWithHttpInfo(List<String> requestBody, { bool? forceRefresh, }) async {
    // ignore: prefer_const_declarations
    final path = r'/api/v1/stock-indices/batch';

    // ignore: prefer_final_locals
    Object? postBody = requestBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    if (forceRefresh != null) {
      queryParams.addAll(_queryParams('', 'forceRefresh', forceRefresh));
    }

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

  /// Get multiple stock indices
  ///
  /// Returns constituent stocks for multiple indices
  ///
  /// Parameters:
  ///
  /// * [List<String>] requestBody (required):
  ///
  /// * [bool] forceRefresh:
  Future<List<NSEStockIndicesDataV1>?> getStockIndicesBatch(List<String> requestBody, { bool? forceRefresh, }) async {
    final response = await getStockIndicesBatchWithHttpInfo(requestBody,  forceRefresh: forceRefresh, );
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      final responseBody = await _decodeBodyBytes(response);
      return (await apiClient.deserializeAsync(responseBody, 'List<NSEStockIndicesDataV1>') as List)
        .cast<NSEStockIndicesDataV1>()
        .toList(growable: false);

    }
    return null;
  }
}
