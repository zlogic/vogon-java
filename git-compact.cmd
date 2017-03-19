SET PATH="C:\Program Files\Git\bin";%PATH%
git gc --prune=now --aggressive
git reflog expire --all
git clean -xdf
