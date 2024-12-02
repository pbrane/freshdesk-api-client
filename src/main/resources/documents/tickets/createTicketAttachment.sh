##Add two attachments to Ticket 7
#curl -v -u "$FD_API_KEY":X \
#  -F "attachments[]=@./bluey.png" \
#  -F "attachments[]=@./Bluey-Wallpaper.jpg" \
#  -X PUT "$FD_BASE_URL/api/v2/tickets/7" |jq

#Add two attachments to Ticket 11 in the Molex sandbox
curl -v -u "$FD_API_KEY":X \
  -F "attachments[]=@./bluey.png" \
  -F "attachments[]=@./Bluey-Wallpaper.jpg" \
  -X PUT "$FD_BASE_URL/api/v2/tickets/11" |jq > sandbox_ticket_11_add_attachments.json

## This creates a new ticket with two attachments
#curl -v -u "$FD_API_KEY":X \
#  -F "attachments[]=@./Bluey-Wallpaper.jpg" \
#  -F "email=david@beaconstrategists.com" \
#  -F "subject=Ticket with Bluey Wallpaper Attachment" \
#  -F "description=This is a ticket with an attachment." \
#  -F "status=2" \
#  -F "priority=1" \
#  -X POST "$FD_BASE_URL/api/v2/tickets" |jq

##Delete an Attachment
#curl -v -u "$FD_API_KEY":X -X DELETE "$FD_BASE_URL/api/v2/attachments/1"

##Try to list attachments, this probably won't work ;)... It definitely doesn't work.
#curl -v -u "$FD_API_KEY":X -X GET "$FD_BASE_URL/api/v2/attachments/153035620444"