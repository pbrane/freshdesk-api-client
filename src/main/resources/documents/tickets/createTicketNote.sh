#Create a note
curl -v -u "$FD_API_KEY":X \
  -H "Content-Type: application/json" \
  -X POST \
  -d '{ "body":"Hi Jimmy, Still working on this.", "private":false, "notify_emails":["david@beaconstrategists.com"] }' \
  "$FD_BASE_URL/api/v2/tickets/7/notes" |jq #> ticket-7-create-a-note.json