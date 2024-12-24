curl -v -u "$FD_API_KEY":X \
  -H "Content-Type: application/json" \
  -X POST \
  -d '{ "body":"Hi Frank, What is the status of this ticket?", "private":false, "notify_emails":["david@beaconstrategists.com"] }' "$FD_BASE_URI/tickets/99/notes" |jq > tacCase99note.json