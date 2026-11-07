# Create the GitHub repository

Suggested name: **family-frame-dashboard**. Choose **Public** to share the source with everyone, or Private for invited collaborators. This package contains the application and documentation, not your personal dashboard backup or adhan recording.

If the repository has already been created for you, clone it instead of running the creation commands again.

## Option A: GitHub CLI on your Mac

Install [GitHub CLI](https://cli.github.com/) and Git if needed. Sign in in your own terminal; do not paste access tokens into chat:

```bash
gh auth login
```

Open the extracted project directory, then initialize and commit:

```bash
cd ~/Downloads/family-frame-dashboard
git init -b main
git add README.md SETUP.md docs scripts tests .github SECURITY.md family-calendar.html .gitignore
git diff --cached --stat
git commit -m "Add working family frame dashboard and setup guide"
gh repo create family-frame-dashboard --public --source=. --remote=origin --push
gh repo view --web
```

If Git asks for your author name/email, configure your own identity (a GitHub-provided noreply email is an option), then retry the commit. If the name already exists, inspect that repository before choosing another name; do not overwrite it.

This creates a repository under the authenticated account. For an organization, use `OWNER/family-frame-dashboard` only after confirming the intended owner and your permissions.

## Option B: GitHub website

1. On [GitHub's new repository page](https://github.com/new), choose your account, name it `family-frame-dashboard`, and select Public.
2. Create an empty repository without generated README/license files.
3. Use Upload files to upload this package's source, docs, scripts and tests; do not upload the ZIP as the only file.
4. Include `.gitignore` when uploading. Never add personal JSON backups, photos, APKs, MP3s or credentials.

## Future changes

Edit the HTML or docs, run the checks and review the diff:

```bash
node tests/check-dashboard.cjs
git diff
git add family-calendar.html README.md SETUP.md docs scripts tests .github SECURITY.md .gitignore
git commit -m "Describe the change"
git push
```

Then update the frame with `bash scripts/install.sh`. Pushing to GitHub does not update installed frames automatically. GitHub Pages is not needed for this local-file deployment; changing to a hosted URL would also change browser storage origin and needs a separate migration plan.

Sources: [GitHub CLI repository creation](https://cli.github.com/manual/gh_repo_create), [GitHub authentication](https://cli.github.com/manual/gh_auth_login), [GitHub commit email settings](https://docs.github.com/en/account-and-profile/how-tos/email-preferences/setting-your-commit-email-address).
