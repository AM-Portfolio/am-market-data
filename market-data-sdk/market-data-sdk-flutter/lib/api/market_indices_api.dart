//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//
// @dart=2.18

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;


class MarketIndicesApi {
  MarketIndicesApi([ApiClient? apiClient]) : apiClient = apiClient ?? defaultApiClient;

  final ApiClient apiClient;

  /// Get all market indices
  ///
  /// Returns all available market indices
  ///
  /// Note: This method returns the HTTP [Response].
  Future<Response> getAllIndicesWithHttpInfo() async {
    // ignore: prefer_const_declarations
    final path = r'/api/v1/indices/all';

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

  /// Get all market indices
  ///
  /// Returns all available market indices
  Future<List<NSEIndexV1>?> getAllIndices() async {
    final response = await getAllIndicesWithHttpInfo();
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      final responseBody = await _decodeBodyBytes(response);
      return (await apiClient.deserializeAsync(responseBody, 'List<NSEIndexV1>') as List)
        .cast<NSEIndexV1>()
        .toList(growable: false);

    }
    return null;
  }

  /// Get index by symbol
  ///
  /// Returns a specific index by its symbol
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [String] symbol (required):
  Future<Response> getIndexWithHttpInfo(String symbol,) async {
    // ignore: prefer_const_declarations
    final path = r'/api/v1/indices/{symbol}'
      .replaceAll('{symbol}', symbol);

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

  /// Get index by symbol
  ///
  /// Returns a specific index by its symbol
  ///
  /// Parameters:
  ///
  /// * [String] symbol (required):
  Future<NSEIndexV1?> getIndex(String symbol,) async {
    final response = await getIndexWithHttpInfo(symbol,);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'NSEIndexV1',) as NSEIndexV1;
    
    }
    return null;
  }
}
