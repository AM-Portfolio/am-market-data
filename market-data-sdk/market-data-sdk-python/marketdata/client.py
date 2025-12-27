import requests
from typing import Dict, Any, Optional

class MarketDataClient:
    """
    Python Client for Market Data API.
    """
    def __init__(self, base_url: str):
        self.base_url = base_url.rstrip('/')
        self.session = requests.Session()

    def get_quotes(self, symbols: str, time_frame: str = "1D") -> Dict[str, Any]:
        """
        Get quotes for symbols.
        
        Args:
            symbols: Comma-separated list of symbols
            time_frame: Timeframe string (e.g., "1D", "5m")
            
        Returns:
            Dict containing quote data
        """
        url = f"{self.base_url}/api/v1/market-data/quotes"
        params = {
            "symbols": symbols,
            "timeFrame": time_frame
        }
        
        response = self.session.get(url, params=params)
        response.raise_for_status()
        return response.json()
