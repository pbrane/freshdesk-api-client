#curl -v -u $FD_API_KEY:X \
#-H "Content-Type: application/json" \
#-X GET '$FD_BASE_URL/api/v2/tickets/4'|jq

#This works
#curl -v -u $FD_API_KEY:X \
#-H "Content-Type: application/json" \
#-X GET '$FD_BASE_URL/api/v2/search/tickets?query="(cf_rma_count:1)"' |jq

#curl -v -u $FD_API_KEY:X \
#-H "Content-Type: application/json" \
#-X GET '$FD_BASE_URL/api/v2/search/tickets?query="(cf_contact_email=david@beaconstrategists.com)"' |jq

#curl -v -u $FD_API_KEY:X \
#-H "Content-Type: application/json" \
#-X GET '$FD_BASE_URL/api/v2/search/tickets?query="(cf_contact_email:%27david@beaconstrategists.com%27)"'

#curl -v -u $FD_API_KEY:X \
#-H "Content-Type: application/json" \
#-X GET '$FD_BASE_URL/api/v2/search/tickets?query="(cf_case_status:%27Acknowledged%27)"' |jq

#curl -v -u $FD_API_KEY:X \
#-H "Content-Type: application/json" \
#-X GET '$FD_BASE_URL/api/v2/search/tickets?query="(cf_business_impact:%27Huge%27)"' |jq

curl -v -u $FD_API_KEY:X \
-H "Content-Type: application/json" \
-X GET '$FD_BASE_URL/api/v2/search/tickets?query="(cf_case_create_date:>%272024-11-28%27)"' |jq
