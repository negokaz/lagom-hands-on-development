# Lagom Hands-On-Development

Lagom のサンプルプロジェクト [Chirper](https://github.com/lagom/activator-lagom-java-chirper)(Twitterライクなアプリケーション) に「お気に入り」機能を追加実装するハンズオンです。

## このハンズオンで必要なもの

* インターネット環境
* ブラウザ ([Google Chrome](https://www.google.co.jp/chrome/browser/desktop/) 推奨)
* ターミナル (CUI)
* [git](https://git-scm.com/)
* [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

## 事前準備

* Java のバージョンが1.8系になっているか確認してください

    ```bash
    $ java -version
    java version "1.8.0_74"
    Java(TM) SE Runtime Environment (build 1.8.0_74-b02)
    Java HotSpot(TM) 64-Bit Server VM (build 25.74-b02, mixed mode)
    $ javac -version
    javac 1.8.0_74
    ```
    JDK8をインストールしたにも関わらず、1.8系になっていない場合はJDK8にパスが通っているか確認してください。
* ターミナルで任意のディレクトリの移動し、リポジトリをクローンします

    ```bash
    $ cd ~/workspace
    $ git clone git@github.com:negokaz/lagom-hands-on-development.git
    ```

* プロジェクトのディレクトリに移動し、開発環境を起動します

    ```bash
    $ cd lagom-hands-on-development
    $ bin/activator ui
    ```
    自動的に [http://localhost:8888](http://localhost:8888) が開き、コードが閲覧できるようになります。(例:  [FavoriteService.java](http://127.0.0.1:8888/app/friend-api/#code/favorite-api/src/main/java/sample/chirper/favorite/api/FavoriteService.java))

    確認できたらターミナル上で`Ctrl + C`を押して終了します。

* 別のターミナルを開き、アプリケーションを起動します

    ```bash
    $ cd lagom-hands-on-development
    $ bin/activator runAll
    ```

    [http://localhost:9000](http://localhost:9000) に移動し、下記のような画面が表示されると起動成功です。
    ![](doc/img/welcome-chiper.png)

    確認できたらターミナル上で`Ctrl + C`を押して終了します。

## ハンズオンを始める

* Activator UI を起動します

    ```bash
    $ cd lagom-hands-on-development
    $ bin/activator ui
    ```

* [チュートリアル](http://127.0.0.1:8888/app/friend-api/#tutorial/0) を確認し、スタッフの指示を待って下さい。
