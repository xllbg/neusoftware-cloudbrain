# 给 E 的 Git 协作说明

仓库地址：`https://github.com/xllbg/neusoftware-cloudbrain`

这个文档是给你复制给 AI 看的操作说明，目标是：让 AI 指导你把仓库拉到本地、创建自己的分支、同步最新代码、提交你的改动、推送到远程，以及和 main 分支保持同步。

## 一、仓库和分支的基本原则

1. 所有人都先拉取 `main`
2. 先从 `main` 新建自己的分支，再在自己的分支上写代码
3. 不要直接在 `main` 上开发
4. 每次开始前先同步 `main`，再切回自己的分支
5. 自己的分支只负责自己的页面或功能

## 二、第一次拉取代码并创建自己的分支

先把仓库克隆到本地：

```bash
git clone https://github.com/xllbg/neusoftware-cloudbrain.git
```

进入项目目录：

```bash
cd neusoftware-cloudbrain
```

切到 main 并拉取最新代码：

```bash
git checkout main
git pull origin main
```

从 main 新建自己的分支，分支名建议：`feature/e-doctor-pages`

```bash
git checkout -b feature/e-doctor-pages
```

如果本地已经有仓库，只是还没有自己的分支，就直接执行上面这三步：

```bash
git checkout main
git pull origin main
git checkout -b feature/e-doctor-pages
```

## 三、日常开发流程

你每天开始写代码前，先同步 main：

```bash
git checkout main
git pull origin main
git checkout feature/e-doctor-pages
git rebase main
```

如果 `rebase` 时出现冲突，先解决冲突，再继续：

```bash
git add .
git rebase --continue
```

如果你不想用 rebase，也可以用 merge：

```bash
git checkout feature/e-doctor-pages
git merge main
```

## 四、提交和推送自己的改动

查看当前改了什么：

```bash
git status
```

把修改加入暂存区：

```bash
git add .
```

提交代码：

```bash
git commit -m "feat: 医生端页面开发"
```

推送到你自己的分支：

```bash
git push origin feature/e-doctor-pages
```

如果是第一次推送这个分支，可以用：

```bash
git push -u origin feature/e-doctor-pages
```

## 五、和 main 合并时怎么做

你自己的分支写完后，不是直接把代码覆盖掉 main，而是发起 Pull Request：

1. 先把分支推到远程
2. 打开 GitHub 仓库
3. 点 Pull requests
4. 新建 PR
5. base 选 `main`
6. compare 选 `feature/e-doctor-pages`
7. 让队友 review
8. 没问题后合并到 `main`

## 六、如果 main 更新了，你的分支怎么办

main 更新后，你的分支不会自动变新，需要手动同步：

```bash
git checkout main
git pull origin main
git checkout feature/e-doctor-pages
git rebase main
```

如果你想保留合并记录，也可以：

```bash
git checkout feature/e-doctor-pages
git merge main
```

## 七、常用命令速查

```bash
git status
git checkout main
git pull origin main
git checkout -b feature/e-doctor-pages
git add .
git commit -m "feat: xxx"
git push origin feature/e-doctor-pages
git rebase main
git merge main
```

## 八、建议你的分支命名

- `feature/e-doctor-pages`
- `feature/e-doctor-ui`
- `feature/e-doctor-record`

## 九、给 AI 的一句话说明

你可以直接把下面这段发给 AI：

> 请指导我在 GitHub 仓库 `https://github.com/xllbg/neusoftware-cloudbrain` 中，先从 `main` 拉取最新代码，再新建自己的分支 `feature/e-doctor-pages`，然后告诉我日常如何同步 main、提交代码、推送分支、以及最后如何发 Pull Request 合并到 main。请只给我可执行的 Git 步骤，不要讲原理太多。

## 十、注意事项

- 不要直接在 `main` 改代码
- 不要把别人的分支当成自己的分支
- 提交前先 `git status`
- 推送前先确认当前分支名
- 合并前先确认没有冲突
