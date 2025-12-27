import unittest
from unittest.mock import Mock, patch
from marketdata.client import MarketDataClient

class TestMarketDataClient(unittest.TestCase):
    def setUp(self):
        self.client = MarketDataClient("http://localhost:8080")

    @patch('requests.Session.get')
    def test_get_quotes(self, mock_get):
        # Arrange
        mock_response = Mock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"NSE:RELIANCE": {"lastPrice": 2500.0}}
        mock_get.return_value = mock_response

        # Act
        result = self.client.get_quotes("NSE:RELIANCE", "1D")

        # Assert
        self.assertEqual(result["NSE:RELIANCE"]["lastPrice"], 2500.0)
        mock_get.assert_called_once()

if __name__ == '__main__':
    unittest.main()
