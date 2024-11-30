#!/bin/zsh
# Query custom fields
# Onion Bagel
curl -v -u $FD_API_KEY:X -X GET '$FD_BASE_URL/api/v2/search/tickets?query="custom_string:%20Onion"'
#curl -v -u $FD_API_KEY:X -X GET '$FD_BASE_URL/api/v2/search/tickets?query="cf_bagel:%27Onion%27"'

