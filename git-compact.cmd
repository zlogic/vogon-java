SET PATH="C:\Program Files\Git\bin";%PATH%
cd vogon-nj-code
git gc --prune=now --aggressive
git reflog expire --all
git clean -xdf
