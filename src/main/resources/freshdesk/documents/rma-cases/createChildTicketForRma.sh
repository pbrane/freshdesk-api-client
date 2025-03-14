#curl -v -u "$FD_API_KEY":X -H "Content-Type: application/json" \
#  -d '{ "email": "jimmy@beaconstrategists.com", "responder_id": 3043029172572, "subject": "OTDR Support Needed Again", "type": "Problem", "source": 3, "status": 2, "priority": 3, "description": "Another OTDR test ticket for me." }' \
#  -X POST "$FD_BASE_URI/tickets" |jq > create_otdr-ticket.json

  curl -v -u "$FD_API_KEY":X -H "Content-Type: application/json" \
    -d '{ "responder_id": 3043029172572, "parent_id": 102, "email": "jimmy@beaconstrategists.com", "subject": "RMA: Fiber issues with device", "type": "Problem", "source": 3, "status": 2, "priority": 3, "description": "Some random fiber signalling issues." }' \
    -X POST "$FD_BASE_URI/tickets" |jq > create_rma-child-ticket-102.json