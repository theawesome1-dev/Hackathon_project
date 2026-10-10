from icalendar import Calendar, Event
from datetime import datetime, timezone
import json

def is_valid_json_file(file_path):
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            json.load(f)
        return True
    except (json.JSONDecodeError, FileNotFoundError, IsADirectoryError):
        return False

# Example usage:
filename = "ai_analysis.json"  # Replace with your JSON file path
with open(filename, 'r', encoding='utf-8') as f:
    response = f.read()

if response.strip().casefold() == "not an event":
    print("The AI response indicates that the content is not an event. Skipping calendar creation.")
    raise SystemExit(0)

try:
    events_data = json.loads(response)
except json.JSONDecodeError:
    print("Error: AI response is neither a JSON event nor 'NOT AN EVENT'.")
    raise SystemExit(1)

if is_valid_json_file(filename):
    print("Valid JSON file.")
    
    cal = Calendar()
    cal.add('prodid', '-//My Calendar Product//mxm.dk//')
    cal.add('version', '2.0')
    print(events_data["title"]);
    event = Event()
    event.add("summary", events_data["title"])
    event.add("description", events_data["description"])
    # Optional fields
    if "organizer" in events_data:
        event.add("organizer", events_data["organizer"])
    if "start" in events_data and "end" in events_data:
        event.add("dtstart", datetime.fromisoformat(events_data["start"]))
        event.add("dtend", datetime.fromisoformat(events_data["end"]))
    if "location" in events_data:
        event.add("location", events_data["location"])
    cal.add_component(event)
    with open('calendar.ics', 'wb') as f:
        f.write(cal.to_ical())
else:
    with open(filename, "r") as file:
        first_line = file.readline()
    if first_line.strip() == "NOT AN EVENT":
        print("The AI response indicates that the content is not an event. Skipping calendar creation.")
    else:
        print("Error: Something went wrong! Invalid JSON file or file not found.")
