import json
import subprocess
import sys
from contextlib import asynccontextmanager
from pathlib import Path


from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
import uvicorn


def run_script(name: str, required: bool = True):
    result = subprocess.run(
        [sys.executable, name],
        capture_output=True,   # needed so result.stderr has content
        text=True,
    )
    print(result.stdout)
    if result.returncode != 0:
        print(f"Error running {name}: {result.stderr}", file=sys.stderr)
        if required:
            sys.exit(result.returncode)


@asynccontextmanager
async def lifespan(app: FastAPI):
    # runs once when the server starts
    # run_script("test_image_caption_link.py")
    # run_script("send_results_to_ai.py")
    yield


# 1. Initialize app FIRST
app = FastAPI(lifespan=lifespan)




def select_event_response(analysis_data):
    if isinstance(analysis_data, dict):
        return [analysis_data] if analysis_data.get("title") else []

    if isinstance(analysis_data, list):
        events = []
        for record in analysis_data:
            if not isinstance(record, dict):
                continue
            analysis = record.get("analysis")
            if isinstance(analysis, dict) and analysis.get("title"):
                events.append(analysis)
        return events

    raise HTTPException(status_code=500, detail="AI analysis has an unsupported JSON format.")


def get_event_response():
    analysis_path = Path(__file__).with_name("ai_analysis.json")
    try:
        analysis_data = json.loads(analysis_path.read_text(encoding="utf-8"))
    except FileNotFoundError as exc:
        raise HTTPException(status_code=404, detail="No AI analysis is available yet.") from exc
    except json.JSONDecodeError as exc:
        raise HTTPException(
            status_code=503,
            detail="AI analysis is invalid. Call /refresh to generate it again.",
        ) from exc
    return select_event_response(analysis_data)


# 2. Define routes AFTER app is created
@app.get("/data")
def get_data():
    return JSONResponse(content=get_event_response())



@app.get("/refresh")
def refresh_data():
    # Re-run your backend collection/AI scripts when requested
    run_script("test_image_caption_link.py")
    run_script("send_results_to_ai.py")

    return JSONResponse(content=get_event_response())


from pydantic import BaseModel

class AccountRequest(BaseModel):
    account: str


def normalize_account(value: str) -> str:
    account = value.strip()
    if not account:
        raise HTTPException(status_code=422, detail="Account cannot be empty.")
    if "\n" in account or "\r" in account:
        raise HTTPException(status_code=422, detail="Account must be a single line.")
    return account


def append_account(account: str, accounts_path: Path) -> None:
    existing_text = accounts_path.read_text(encoding="utf-8-sig") if accounts_path.exists() else ""
    with accounts_path.open("a", encoding="utf-8", newline="") as accounts_file:
        if existing_text and not existing_text.endswith(("\n", "\r")):
            accounts_file.write("\n")
        accounts_file.write(account + "\n")


def remove_account(account: str, accounts_path: Path) -> int:
    if not accounts_path.exists():
        return 0

    existing_accounts = accounts_path.read_text(encoding="utf-8-sig").splitlines()
    remaining_accounts = [
        line for line in existing_accounts
        if line.strip() and line.strip().casefold() != account.casefold()
    ]
    removed_count = len(existing_accounts) - len(remaining_accounts)
    accounts_path.write_text(
        "\n".join(remaining_accounts) + ("\n" if remaining_accounts else ""),
        encoding="utf-8",
    )
    return removed_count

@app.post("/update")
def update_accounts(request: AccountRequest):
    account = normalize_account(request.account)
    accounts_path = Path(__file__).with_name("accounts.txt")
    append_account(account, accounts_path)

    return {
        "status": "success",
        "message": f"Added {account} to accounts.txt",
        "account": account,
    }


@app.post("/delete")
def delete_account(request: AccountRequest):
    account = normalize_account(request.account)
    accounts_path = Path(__file__).with_name("accounts.txt")
    removed_count = remove_account(account, accounts_path)
    if not removed_count:
        raise HTTPException(status_code=404, detail=f"Account {account} was not found.")

    return {
        "status": "success",
        "message": f"Removed {account} from accounts.txt",
        "removed": removed_count,
    }


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)