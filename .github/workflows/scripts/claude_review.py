import os
import sys
import json
import requests

API_KEY = os.environ["ANTHROPIC_API_KEY"].strip()

# Read the PR diff
with open("pr.diff") as f:
    diff = f.read()

# Truncate large diffs to stay within API limits (~100K chars)
MAX_DIFF_SIZE = 100000
if len(diff) > MAX_DIFF_SIZE:
    diff = diff[:MAX_DIFF_SIZE] + "\n\n... [diff truncated due to size] ..."

if not diff.strip():
    print("No diff found, skipping review.")
    with open("review.md", "w") as f:
        f.write("No code changes detected in this PR.")
    sys.exit(0)

prompt = f"""You are a senior Java/Spring Boot engineer performing a code review on a microservices e-commerce platform.

Review this pull request diff and provide a structured review in Markdown format:

## Summary
Brief overview of the changes.

## Issues Found

### Bugs
List any bugs or logic errors.

### Security Concerns
List any security vulnerabilities (SQL injection, XSS, auth issues, exposed secrets, etc.).

### Performance Issues
List any performance concerns (N+1 queries, missing indexes, inefficient algorithms, etc.).

### Code Quality
List any code style problems, missing error handling, or violations of Spring Boot best practices.

## Suggestions
Actionable suggestions for improvement.

If no issues are found in a category, write "None found."

PR DIFF:
```diff
{diff}
```"""

response = requests.post(
    "https://api.anthropic.com/v1/messages",
    headers={
        "x-api-key": API_KEY,
        "anthropic-version": "2023-06-01",
        "content-type": "application/json",
    },
    json={
        "model": "claude-sonnet-4-5-20250514",
        "max_tokens": 4096,
        "messages": [{"role": "user", "content": prompt}],
    },
    timeout=120,
)

if response.status_code != 200:
    print(f"API error ({response.status_code}): {response.text}", file=sys.stderr)
    sys.exit(1)

data = response.json()
if "content" not in data or not data["content"]:
    print(f"Unexpected API response: {json.dumps(data, indent=2)}", file=sys.stderr)
    sys.exit(1)

review = data["content"][0]["text"]

print(review)

# Save review output as Markdown
with open("review.md", "w") as f:
    f.write(review)
