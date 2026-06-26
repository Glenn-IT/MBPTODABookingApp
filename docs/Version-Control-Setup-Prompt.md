# Version Control Setup Prompt — VB.NET Windows Forms (Reusable)

Copy and paste this prompt to use the same version control system on any VB.NET Windows Forms project in Visual Studio 2022.

---

## The Prompt

```
I want to set up a week-by-week versioned presentation system for this project using Git and GitHub.
This is a VB.NET Windows Forms project in Visual Studio 2022.

Before doing anything, scan the entire project and identify:
- All Forms available in the system
- What each Form does (brief description)
- Group them by feature or role (e.g. admin forms, user forms, public forms)

Once you have a complete picture of all the features and forms, create a rollout plan like this:
- v1.00 starts with the most basic feature (Login and Register, or the landing form)
- Each version after that unlocks exactly ONE form or feature on top of the previous version
- Do admin forms first (one per version), then user/patient forms (one per version)
- The final version presents the full system with everything unlocked
- Forms not yet presented in the current version should show an "Under Construction" placeholder

Use this version numbering format: v1.00, v1.01, v1.02 ... up to however many forms you have.

Present a plan first in this format and wait for my approval:

  v1.00 - Login / Register - Admin/User
  v1.01 - Admin: [Feature]
  v1.02 - Admin: [Feature]
  ...
  vX.XX - User: [Feature]
  ...
  vX.XX - User: [Last Feature] (Full System)

After I approve the plan, do the following:

1. Create `UnderConstructionForm.vb` — a Windows Form that acts as the gate:
   - Add a module-level constant at the top: `Public Const CURRENT_VERSION As String = "v1.00"`
   - Style the form with a dark blue background (BackColor = #1A237E)
   - Add these controls:
       - A Label with the hard-hat emoji 🚧 (large font, centered)
       - A Label showing "Current Version: " & CURRENT_VERSION (orange text, centered)
       - A Label with the title "Under Construction" (white, bold, large)
       - A Label with description "This feature is not yet available in the current presentation version."
       - A Button "← Go Back" that calls Me.Close()
   - Do NOT call Application.Exit — only close this form so the caller can also close itself

2. Gate each locked Form by adding these 4 lines at the very top of its Form_Load event handler:
   ```vb
   Dim gate As New UnderConstructionForm()
   gate.ShowDialog()
   Me.Close()
   Return
   ```
This shows the Under Construction form, waits for it to close, then closes the gated form.

3. Forms that are part of the current version (v1.00) should NOT have the gate.

4. Create a `docs/Version-Control.md` file with:
    - The full rollout schedule table (version, feature, forms unlocked, forms still gated)
    - The Under Construction strategy explanation
    - Git commands to use per version
    - How Git tags work as permanent snapshots
    - A GitHub Release Tags table with version, tag name, and commit hash columns (fill hashes at the end)
    - A section on what to do when a prof or client requests changes after a presentation

5. For each version, when I say "Proceed":
    - Remove the 4 gate lines from the Form_Load of the form being unlocked that version
    - Update CURRENT_VERSION in UnderConstructionForm.vb to the new version string
    - Commit with: feat: implement vX.XX - unlock [Feature Name]
    - Create a Git tag: git tag vX.XX
    - Push both: git push origin main && git push origin vX.XX
    - Do NOT stop to ask for confirmation between versions — keep going until all versions are done
    - After ALL versions are committed and pushed, update the commit hash column in Version-Control.md
      using: git tag | sort | xargs -I{} git log -1 --format="{} %H" {}
      Then commit and push the updated docs.

6. If any form has live data that should not be visible yet, add a gate that zeroes out the data
   and shows an empty "Under Construction / No records to show" state instead of hiding the whole form.
   Remove this data gate when the relevant version is reached.

Present the plan first and wait for my approval before making any changes.
```

---

## Notes for Using This Prompt

- This prompt is written for **VB.NET Windows Forms** projects in Visual Studio 2022. For other languages, see the matching prompt file in this folder:
  - `Version-Control-Setup-Prompt.md` — PHP projects

- The version numbering used here is `v1.00, v1.01, v1.02 ...`. The number of versions equals
  the number of forms/features in your project — one unlock per version.

- If your project has both an **admin side** and a **user/patient side**, mention that in the prompt
  so the AI groups features correctly (admin forms first, user forms after).

- When the AI asks "Proceed" for the next version, you can say **"Proceed without asking for
  confirmation, do all remaining versions at once"** to let it finish everything in one go.

- After all versions are done, the AI will automatically fill in the commit hash table in
  `docs/Version-Control.md` and push the final update.

---

## How the Gate Works in VB.NET

When a gated form loads, it immediately shows `UnderConstructionForm` as a dialog (blocking), then closes itself:

```vb
Private Sub FormDashboard_Load(sender As Object, e As EventArgs) Handles MyBase.Load
    ' GATE — remove this block when unlocking for vX.XX
    Dim gate As New UnderConstructionForm()
    gate.ShowDialog()
    Me.Close()
    Return
    ' END GATE

    ' ... real form code below ...
End Sub
```

To **unlock** a form for a version:
1. Delete the 4 gate lines from its `Form_Load`
2. Update `CURRENT_VERSION` in `UnderConstructionForm.vb`
3. Commit, tag, and push

---

## Sample Rollout Format (for reference)

```
Version 1.00 - Login / Register - Admin/User

Version 1.01 - Admin
- Dashboard

Version 1.02 - Admin
- Manage Appointments

Version 1.03 - Admin
- View Calendar

Version 1.04 - Admin
- Doctor Schedule

Version 1.05 - Admin
- Patient Records

Version 1.06 - Admin
- Manage Users

Version 1.07 - Admin
- Reports

Version 1.08 - Admin
- Admin Profile

Version 1.09 - User
- Dashboard

(continue one per menu for user side)
```

Each form's own features must work fully when unlocked.
If a button inside it opens a form not yet unlocked, that form will naturally
show the Under Construction gate when it loads.

---

## Quick Reference: Commands Per Version

```bash
# Stage and commit
git add <UnlockedForm.vb> UnderConstructionForm.vb
git commit -m "feat: implement vX.XX - unlock [Feature]"

# Tag and push
git tag vX.XX
git push origin main
git push origin vX.XX
```

## Quick Reference: Update Docs After All Versions Are Done

```bash
# Get all tag hashes to paste into Version-Control.md
git tag | sort | xargs -I{} git log -1 --format="{} %H" {}
```

## Quick Reference: When Prof Requests Changes After a Presentation

```bash
# Fix on main first
git checkout main
git add .
git commit -m "feat: update [form] per feedback"
git push origin main

# Delete old tag and re-create it pointing to the new commit
git tag -d vX.XX
git push origin :refs/tags/vX.XX
git tag vX.XX
git push origin vX.XX
```