# Quem

Quest manager for reincarnation-pve

![Version](https://img.shields.io/badge/version-0.1.0-blue?style=flat-square)
![Licence](https://img.shields.io/badge/licence-GPL--3.0-red?style=flat-square)

Quem は [アジ鯖](https://www.azisaba.net) の Reincarnation PvE サーバー用に開発されたクエストマネージャーです。

## 設定ファイル

初回起動時に生成される `./plugins/quem/config.yml` がプラグインの設定ファイルになります。

`/quem reload` で設定ファイルとすべての名前空間を再読み込みします。

```yaml
# パーティーの最大サイズ
maxPartySize: 8

# パーティー招待の有効期限 (tick数)
partyInviteLimit: 1200

# クエスト終了後にテレポートする場所
# 指定しない場合にはクエスト開始前の場所にテレポートします
lobby:
  world: 'minecraft:overworld'
  x: 0
  y: 0
  z: 0
  yaw: 0 # 任意
  pitch: 0 # 任意

# クエストで表示するパネルの設定
# & 記号で装飾できます
panel:
  title: '&eReincarnation'
  footer: '&7いますぐ &eazisaba.net&7 で遊べ！'
```

## コマンド

```shell
# 任意のプレイヤーにクエストタイプを解放します
/quem grant <player(s)> <quest type>

# 任意のクエストの進捗を変更します
/quem progress <player> <requirement> <formula>
# formula は 「+2」「-3」「*5」「/7」「=11」のような形式で記述します
# formula で式のはじめの演算子を省略した場合、「=」として扱われます
# formula はダブルクォーテーションで「"*2"」のように囲むことが推奨されます

# 設定ファイルとすべての名前空間を再読み込みします
/quem reload

# 任意のプレイヤーからクエストタイプをはく奪します
/quem revoke <player(s)> <quest type>

# 任意のクエストをステージにマウント/アンマウントします
/quem stage <mount/unmount> <player(s)> <stage>
```

## 名前空間

名前空間はこのあと紹介するクエストタイプやクエストカテゴリ、ステージといったオブジェクトを分類します。

Quem は各種オブジェクトを `名前空間:キー` の完全修飾名で管理します。

### 使用できる文字

数字: `0-9`,
英小文字: `a-z`,
下線: `_`,
ハイフン: `-`

### 定義の方法

名前空間は `./plugins/quem/` に `@名前空間` の名前でディレクトリを作成することで定義されます。

## クエストタイプ

クエストタイプはクエストの種別を表すオブジェクトです。

### なぜクエスト「タイプ」と呼ぶのですか？

Quem で「クエスト」という語はプレイヤーが定義された目標に挑戦する一連の流れを指します。
つまり、同じ種類のクエストが2つ以上同時に発生することがあります。

これに対して、「クエストタイプ」は種類そのものを意味し、常に一意です。

全てのクエストは1つのクエストタイプを持ち、クエストタイプでの定義を参照しながら進行します。

### Grant と Revoke

プレイするにはそのクエストタイプを解放している必要があります:

```shell
/quem grant <player> <quest type>
/quem revoke <player> <quest type>
```

### 定義の方法

名前空間ディレクトリ下に `.yml` 拡張子でファイルを作成します。

名前空間ディレクトリ直下にディレクトリを作成してファイルを分類することも可能です。

`名前空間:拡張子をのぞいた名前空間ディレクトリからの相対パス` として登録されます。

```yaml
# プレイヤーに表示するときに使用されます. & 記号で装飾できます.
title: '&aタイトル'

# プレイヤーに表示するときに使用されます
icon:
  type: 'minecraft:diamond'
  aura: true # エンチャントのオーラを適用するかを定義します. 規定値は false で指定は任意です.

# プレイヤーに表示するときに使用されます. & 記号で装飾できます.
description:
  - '&7これは一行目です'
  - '&8これは二行目です'

# 名前空間付きキーでカテゴリを指定します
# ビルドインのカテゴリ:
# quem:general 最も一般的なクエストタイプであることを意味します
# quem:daily デイリークエストであることを意味します. プレイヤーにはこのカテゴリに属する物の中から毎日ランダムに抽選されてたものが表示されます.
# quem:story ストーリーに関連するクエストタイプであることを意味します
category: 'quem:general'

# クエストの開始位置を定義します
location:
  world: 'minecraft:overworld'
  x: 0
  y: 0
  z: 0
  yaw: 0 # 任意
  pitch: 0 # 任意

# クリア可能な回数を定義します. 指定は任意で、定義しない場合は上限は発生しません.
maxPlays: 6

# 挑戦可能な最大パーティーサイズを指定します. 指定は任意で、定義しない場合は上限は発生しません.
maxPlayers: 6

# 挑戦可能な最小パーティーサイズを指定します. 指定は任意で、定義しない場合は下限は発生しません.
minPlayers: 3

# ガイドを定義します
guides:
  # 条件が満たされるガイドが複数ある場合は、guides リストで上位にあるものが優先されます
  - title: '&aチェックポイント' # 指定は任意です. & 記号で装飾できます.
    location: # 場所
      world: 'minecraft:overworld'
      x: 0
      y: 0
      z: 0
      yaw: 0 # 任意
      pitch: 0 # 任意
    requirements: # このガイドが選択されるのに必要な条件
      # この例では requirement1 が 1 かつ requirement2 が 1 であるときに選択されます
      requirement1: 1
      requirement2: 1

# 達成に必要な条件
requirements:
  # requirement名: 必要な値 のセットで定義します
  requirement1: 3
  requirement2: 5

# トリガーに応じて実行されるスクリプト
# 次のトリガーがあります:
# start クエストが開始されたときに呼び出されます
# end 理由を問わずクエストが終了したときに呼び出されます
# complete クエストが達成された終了したときに呼び出されます
# cancel クエストが達成されずに終了したときに呼び出されます
scripts:
  start: # トリガーをキーとする文字列リストでスクリプトを表現します
    - 'command arg1 arg2'
  start+20: # トリガーに「+整数」を続けることで、tick数で遅延を指定できます
    - 'command arg1 arg2'
  complete:
    - ':quem grant % rcpve:example' # これはコマンドの拡張構文です. 「:」から開始されたコマンドは、クエストのメンバー数だけ繰り返し実行されます.このとき「%」部分はプレイヤー名に置き換えられます.
```

## クエストカテゴリ

クエストカテゴリはクエスト選択画面でタブとして表示されます。

ビルドインの `quem:general` `quem:daily` `quem:story` のほかに追加で定義することができます。

たとえば、イベントなどで期間限定のクエストタイプを追加するときには、イベント専用のクエストカテゴリを作成するとよいでしょう。

### 定義の方法

名前空間ディレクトリ下に `.yml` 拡張子でファイルを作成します。

名前空間ディレクトリ直下にディレクトリを作成してファイルを分類することも可能です。

`名前空間:拡張子をのぞいた名前空間ディレクトリからの相対パス` として登録されます。

```yaml
# & 記号で装飾できます
title: '&a春イベント'

# タブでのアイコンを定義します
icon:
  type: 'minecraft:cherry_sapling'
  aura: true # エンチャントのオーラを適用するかを定義します. 規定値は false で指定は任意です.
```

## ステージ

ステージは指定した数のパーティーだけが同時に挑戦できる要素です。

ボス戦など、同時に挑戦できるパーティーを制限したい場面で使用します。

### コマンド

```shell
# プレイヤーのパーティーをステージにマウントします. 満員の場合はキューに追加します.
/quem stage mount <player> <stage>

# プレイヤーのパーティーをステージからアンマウントします
/quem stage unmount <player> <stage>
```

### 定義の方法

名前空間ディレクトリ下に `.yml` 拡張子でファイルを作成します。

名前空間ディレクトリ直下にディレクトリを作成してファイルを分類することも可能です。

`名前空間:拡張子をのぞいた名前空間ディレクトリからの相対パス` として登録されます。

```yaml
# & 記号で装飾できます
title: '&aボス戦'

# 開始場所を指定します
location:
  world: 'minecraft:overworld'
  x: 0
  y: 0
  z: 0
  yaw: 0 # 任意
  pitch: 0 # 任意

# 終了場所を指定します. 指定は任意で、定義しない場合は開始前の場所にテレポートします。
unmountLocation:
  world: 'minecraft:overworld'
  x: 0
  y: 0
  z: 0
  yaw: 0 # 任意
  pitch: 0 # 任意

# 同時に挑戦できるパーティーの最大数を指定します. 指定は任意で、定義しない場合は 1 として扱われます.
maxParties: 1
```