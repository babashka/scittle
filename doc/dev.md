# Dev

## Release

Static files including compiled JS are hosted on Github. This is set up like
described
[here](https://medium.com/linagora-engineering/deploying-your-js-app-to-github-pages-the-easy-way-or-not-1ef8c48424b7):

All the commands below assume that you already have a git project initialized and that you are in its root folder.

```
# Create an orphan branch named gh-pages
git checkout --orphan gh-pages
# Remove all files from staging
git rm -rf .
# Create an empty commit so that you will be able to push on the branch next
git commit --allow-empty -m "Init empty branch"
# Push the branch
git push origin gh-pages
```

Now that the branch is created and pushed to origin, let’s configure the worktree correctly:

```
# Come back to master
git checkout main
# Add gh-pages to .gitignore
echo "gh-pages/" >> .gitignore
git worktree add gh-pages gh-pages
```

After cloning this repo to a new dir:

```
git fetch origin gh-pages
git worktree add gh-pages gh-pages
```

To deploy to Github Pages:

```
script/release
```

To create a new release:

```
cd gh-pages
git checkout -b v0.0.2
git push --set-upstream origin v0.0.2
```

Then make a new release on Github with the `v0.0.2` tag.

To upgrade examples:

```
rg '0.0.1' --files-with-matches | xargs sed -i '' 's/0.0.1/0.0.2/g'
```
