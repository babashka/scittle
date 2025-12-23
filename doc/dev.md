# Dev

## Workflow

### Start with an issue before writing code

Before writing any code, please create an issue first that describes the problem
you are trying to solve with alternatives that you have considered. A little bit
of prior communication can save a lot of time on coding. Keep the problem as
small as possible. If there are two problems, make two issues. We discuss the
issue and if we reach an agreement on the approach, it's time to move on to a
PR.

### Follow up with a pull request

Post a corresponding PR with the smallest change possible to address the
issue. Then we discuss the PR, make changes as needed and if we reach an
agreement, the PR will be merged.

<!-- ### Tests -->

<!-- Each bug fix, change or new feature should be tested well to prevent future -->
<!-- regressions. -->

### Force-push

Please do not use `git push --force` on your PR branch for the following
reasons:

- It makes it more difficult for others to contribute to your branch if needed.
- It makes it harder to review incremental commits.
- Links (in e.g. e-mails and notifications) go stale and you're confronted with:
  this code isn't here anymore, when clicking on them.
- CircleCI doesn't play well with it: it might try to fetch a commit which
  doesn't exist anymore.
- Your PR will be squashed anyway.

## Developing

Run `bb dev` to start shadow-cljs compilation in watch mode.

<!-- ## Testing -->

<!-- You can run tests using `bb run-tests` and `bb run-integration-tests`. -->

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
git worktree prune
git worktree add gh-pages gh-pages
```

To deploy to Github Pages:

```
script/release.clj
```

To create a new NPM release:

- Prepare version `package.json`, except patch (if anything should change here)
- Run `bb npm-publish`: this will compile, bump patch version, create tag and and push to npm and Github
- `bb replace-version 0.6.16 0.7.30`
- Create Github release with updated links from `doc/links.md`
- `bb gh-pages`

<!-- To upgrade examples: -->

<!-- ``` -->
<!-- rg '0.0.1' --files-with-matches | xargs sed -i '' 's/0.0.7.30.1.0/g' -->
<!-- bb release -->
<!-- cd gh-pages -->
<!-- git checkout -b v0.7.30 -->
<!-- git push --set-upstream origin v0.7.30 -->
<!-- git checkout gh-pages -->
<!-- cd .. -->
<!-- ``` -->

<!-- Then make a new release on Github with the `v0.7.30` tag. -->
