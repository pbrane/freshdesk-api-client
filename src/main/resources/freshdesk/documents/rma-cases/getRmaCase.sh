#curl -v -u $FD_API_KEY:X \
#  -H "Content-Type: application/json" \
#  -X GET "$FD_BASE_URL/api/v2/custom_objects/schemas/10498799/records/_1-3" |jq > ticket-7-tac-case_1-3.json

#Molex Sandbox Ticket 10 and related TAC Case
curl -v -u $FD_API_KEY:X \
  -H "Content-Type: application/json" \
  -X GET "$FD_BASE_URI/custom_objects/schemas/10733585/records?ticket=138" |jq > rma_case_for_child_ticket_138.json
