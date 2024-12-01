#Get Test Ticket 7 and include conversations (only supports the first 10 conversations)
#curl -v -u "$FD_API_KEY":X \
#-H "Content-Type: application/json" \
#-X GET "$FD_BASE_URL/api/v2/tickets/7?include=conversations" |jq > ticket-7-having-notes-and-replies.json

#List Test Ticket 7's conversation
curl -v -u "$FD_API_KEY":X \
  -H "Content-Type: application/json" \
  -X GET "$FD_BASE_URL/api/v2/tickets/7/conversations" |jq > ticket-7-conversation.json

##Get a note from the conversation of ticket 7 (Doesn't work: 404)
#curl -v -u "$FD_API_KEY":X \
#  -H "Content-Type: application/json" \
#  -X GET "$FD_BASE_URL/api/v2/tickets/7/conversations/153075655125" #|jq

#Create a note
#curl -v -u "$FD_API_KEY":X \
#  -H "Content-Type: application/json" \
#  -X POST \
#  -d '{ "body":"Hi Jimmy, Still working on this.", "private":false, "notify_emails":["david@beaconstrategists.com"] }' "$FD_BASE_URL/api/v2/tickets/7/notes" |jq > ticket-7-create-a-note-1.json

#Try to get a note (doesn't work)
#curl -v -u "$FD_API_KEY":X \
#  -H "Content-Type: application/json" \
#  -X GET "$FD_BASE_URL/api/v2/tickets/7/notes/153075656929" |jq #> try-to-get-a-note-by-id.json


#Not Supported, can only create notes with this endpoint
#curl -v -u "$FD_API_KEY":X \
#  -H "Content-Type: application/json" \
#  -X GET "$FD_BASE_URL/api/v2/tickets/3/notes" |jq





#curl -v -u "$FD_API_KEY":X \
#-H "Content-Type: application/json" \
#-X GET "$FD_BASE_URL/api/v2/tickets/4" |jq > ticket-4.json
#

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

#curl -v -u $FD_API_KEY:X \
#-H "Content-Type: application/json" \
#-X GET '$FD_BASE_URL/api/v2/search/tickets?query="(cf_case_create_date:>%272024-11-28%27)"' |jq
