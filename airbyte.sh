#!/bin/bash
WEBFLOW_URL="https://api.webflow.com/collections/631aeff93e54dc14b555344a/items"
WEBFLOW_AUTHOR="Person_5e22e90adaefb87e2849af40"
RES="jvm/src/main/resources/airbyte"
CONNECTORS="/tmp/airbyte/airbyte-integrations/connectors"

# rm -fr $RES
# mkdir -p $RES
# rm -fr /tmp/airbyte
# git clone --depth=1 https://github.com/airbytehq/airbyte.git /tmp/airbyte

# # Remove unneeded integrations
# rm -fr $CONNECTORS/*scaffold*
# rm -fr $CONNECTORS/*test*
# rm -fr $CONNECTORS/*tutorial*
rm -fr /tmp/connectors*

# Cache list of existing Webflow connectors
x=0
while [ $x -le 5 ]
do
  OFFSET=$(( $x * 100))
  echo "Caching connectors at offset $OFFSET"
  curl -o /tmp/connectors_$x -s --request GET \
    --url "$WEBFLOW_URL?live=true&offset=$OFFSET" \
    --header "Accept-Version: 1.0.0" \
    --header "Authorization: Bearer $WEBFLOW_TOKEN"
  x=$(( $x + 1 ))
done

cat /tmp/connectors* | jq -n '{ items: [ inputs.items ] | add }' > /tmp/connectors

# For each of the connectors
for dir in $CONNECTORS/*; do
    SPEC_PATH=$(find $dir -name spec.json)
    if [[ "$SPEC_PATH" == *airbyte* ]]; then

        # Get connector info
        SPEC_NAME=$(echo "$SPEC_PATH" | cut -d/ -f6)
        DIRECTION=(${SPEC_NAME//-/ }[1])
        TITLE=$(cat $SPEC_PATH | jq -r '.connectionSpecification.title' | sed -e 's/ Spec//g' | sed -e 's/ Destination//g' | sed -e 's/Destination //g' | sed -e 's/ Source//g' | sed -e 's/Source //g')
        TITLE=$(echo $TITLE | sed -e 's/ Connection Configuration//g' | sed -e 's/ Configuration//g')
        REQUIRED=$(cat $SPEC_PATH | jq -r '.connectionSpecification.required')
        PARAMETERS="$(cat $SPEC_PATH | jq -r 'include "airbyte"; airbyteParameters')"
        PARAMETERS_TABLE="<table class='parameters'><tr><th style="width:25%">Name</th><th>Type</th><th>Description</th></tr>"$(echo "$PARAMETERS" | sed -e 's#|#</td><td>#g' | sed -e 's#^#</td></tr><tr><td>#g' | sed -e 's#"#\\"#g' )"</td></tr></table>"

        WEBFLOW_DATA="{
            \"fields\": {
              \"name\": \"$TITLE\",
              \"slug\": \"$SPEC_NAME\",
              \"direction\": \"$DIRECTION\",
              \"parameters\": \"$PARAMETERS_TABLE\",
              \"_archived\": false,
              \"_draft\": false
            }
          }"

        echo $WEBFLOW_DATA > /tmp/connector_data

        # Create or update connector in Webflow
        WEBFLOW_ITEM=$(cat /tmp/connectors | jq ".items | map(select(.slug == \"$SPEC_NAME\"))[0]")
        if [[ $WEBFLOW_ITEM == "null" ]]; then
          echo "Creating $SPEC_NAME"
          curl -s --request POST \
            --url "$WEBFLOW_URL?live=true" \
            --header "Accept-Version: 1.0.0" \
            --header "Authorization: Bearer $WEBFLOW_TOKEN" \
            --header "Content-Type: application/json" \
            --data-binary "@/tmp/connector_data"
        else
          ITEM_ID=$(echo $WEBFLOW_ITEM | jq -r "._id")
          echo "Updating $SPEC_NAME with id: $ITEM_ID"
          
          curl -s --request PUT \
            --url "$WEBFLOW_URL/$ITEM_ID?live=true" \
            --header "Accept-Version: 1.0.0" \
            --header "Authorization: Bearer $WEBFLOW_TOKEN" \
            --header "Content-Type: application/json" \
            --data-binary "@/tmp/connector_data"
        fi

        # Copy JSON file
        cp $SPEC_PATH $RES/$SPEC_NAME.json

        # Add to localisation file
        KEYS=$(jq -r '[. as $in | (paths(scalars), paths((. | length == 0)?)) | join(".") as $key | $key + "=" + ($in | getpath($key | split(".") | map((. | tonumber)? // .)) | tostring) ] | sort | .[]' $SPEC_PATH)

        TITLES=$(echo "$KEYS" | grep "title=" | grep properties | sed -e "s/connectionSpecification.properties/$SPEC_NAME/g")
        S_TITLES=$(echo "$TITLES" | sed -e "s/source-/datasources.section.source-/g")
        D_TITLES=$(echo "$S_TITLES" | sed -e "s/destination-/datasources.section.destination-/g")
        echo "$D_TITLES" | grep -v "connectionSpecification" | grep -v "scaffold-" | sed -e '/^$/d' >> "$RES/messages_airbyte_en"

        DESCRIPTIONS=$(echo "$KEYS" | grep "description=" | grep properties | sed -e "s/connectionSpecification.properties/$SPEC_NAME/g")
        S_DESCRIPTIONS=$(echo "$DESCRIPTIONS" | sed -e "s/source-/datasources.section.source-/g")
        D_DESCRIPTIONS=$(echo "$S_DESCRIPTIONS" | sed -e "s/destination-/datasources.section.destination-/g")
        echo "$D_DESCRIPTIONS" | grep -v "connectionSpecification" | grep -v "scaffold-" | sed -e '/^$/d' >> "$RES/messages_airbyte_en"
    fi
done

# echo "Removing references to Airbyte .."
# sed -i '' 's/airbyte.io/harana.com/g' "$RES/messages_airbyte_en"
# sed -i '' 's/airbyte/harana/g' "$RES/messages_airbyte_en"
# sed -i '' 's/airbytehq/harana/g' "$RES/messages_airbyte_en"
# sed -i '' 's/Airbyte/harana/g' "$RES/messages_airbyte_en"
# sed -i '' 's/ (Optional)//g' "$RES/messages_airbyte_en"