curl -v -u "$FD_API_KEY":X \
  -H "Content-Type: application/json" \
  -X GET \
  "$FD_BASE_URI/tickets/99/conversations" |jq > tacCase99Conversations.json
