#curl -v -u $FD_API_KEY:X \
#-H "Content-Type: application/json" \
#-X GET "$FD_BASE_URI/search/tickets?query=(created_at: > %272024-11-28%27)" |jq

#This works too
curl -v -u $FD_API_KEY:X -X GET "$FD_BASE_URI/search/tickets?query=%22created_at:%3E'2025-03-07'%20AND%20created_at:%3C'2025-03-08'%22&page=1" |jq > queryTickets.json
#with a TAG
#curl -v -u $FD_API_KEY:X -X GET "$FD_BASE_URI/search/tickets?query=%22created_at:%3E'2025-03-01'%20AND%20created_at:%3C'2025-03-23'%20AND%20tag:'TAC'%22&page=1" |jq > queryTickets.json

#This works
#curl -v -u $FD_API_KEY:X -X GET "$FD_BASE_URI/search/tickets?query=%22created_at:>%272025-01-01%27%20AND%20created_at:<%272025-02-01%27%22&page=1" |jq


#curl -v -u $FD_API_KEY:X -X GET "$FD_BASE_URI/search/tickets?query=created_at:>%272024-12-31%27%20AND%20created_at:<%272025-02-01%27" |jq
#curl -v -u $FD_API_KEY:X -X GET "$FD_BASE_URI/search/tickets?query=due_by:>%272017-10-01%27%20AND%20due_by:<%272017-10-07%27" |jq

#"created_at:>%272024-12-31%27%20AND%20created_at:<%272025-02-01%27"
#"due_by:>%272017-10-01%27%20AND%20due_by:<%272017-10-07%27"

#This works
#curl -v -u $FD_API_KEY:X -X GET "$FD_BASE_URI/search/tickets?query=%22created_at:>%272025-03-20%27%22" |jq

#This works
#curl -v -u $FD_API_KEY:X -X GET "$FD_BASE_URI/search/tickets?query=%22created_at:>%272025-03-20%27%22" |jq
