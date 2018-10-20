
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

public class Client extends JFrame implements ActionListener{
	//定数
	public static final int WINDOW_X = 960; //ウィンドウサイズ横
	public static final int WINDOW_Y = 540; //ウィンドウサイズ縦

	//サーバ
	Socket socket = null;
	private PrintWriter out; //データ送信用オブジェクト
	private Receiver receiver; //データ受信用オブジェクト

	//部品
	Container c = getContentPane();
	JLabel label1, label2, label3, label4, label5, label6,label7, label8, label9, label10;
	JLabel[] arrayLabel;
	JLabel backG1, backG2, backG3, backG4, backG5;
	JButton button1, button2, button3, button4, button5, button6, button7, button8, button9, button10;
	JButton[] arrayButton;
	JTextField text1, text2, text3, text4, text5;
	EtchedBorder border; //枠線
	ImageIcon blackIcon, whiteIcon, boardIcon;//アイコン

	JTextArea disparea;
    StringBuilder sb;
    JScrollPane scrollpane1;

	//変数
	private String ipAddress = "";
	private String port = "";
	private int portNum = 0;
	public int winFlag = 0; //画面遷移フラグ
	private int x, y; //座標
	private int num = 0;
	private String userInfo;
	private Player myUser, opUser;
	private int matchingUserNum = 0;
	private Player[] matchingUser;
	private Othello othello; //Othelloオブジェクト
	private int row = 8; //行数
	private String myStr, opStr;
	private boolean passFlag = false;
	private boolean faultFlag = false;
	private boolean surrenderFlag = false;
	private boolean gameFlag = false;
	private boolean serverFlag = true;
	private String gameResult = "";

	//コンストラクタ
	public 	Client() {
		//ウィンドウ設定
		setTitle("ネットワーク対戦型オセロ"); //タイトル
		setSize(WINDOW_X, WINDOW_Y); //サイズ
		setLocationRelativeTo(null); //画面中央に表示
		setVisible(true); //表示
		setLayout(null); //レイアウトマネージャーを無効
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//枠線設定
		border = new EtchedBorder(EtchedBorder.RAISED, Color.white, Color.black);

		//アイコン
		whiteIcon = new ImageIcon("../pic/White.jpg");
		blackIcon = new ImageIcon("../pic/Black.jpg");
		boardIcon = new ImageIcon("../pic/GrreenFrame.jpg");

		//背景
		try {
	        backG1 = new JLabel(new ImageIcon(ImageIO.read(new File("../pic/back1.jpg"))));
	        backG1.setBounds(0, 0, WINDOW_X, WINDOW_Y);
		}
        catch (IOException e) {
        	e.printStackTrace();
        }
        try {
	        backG2 = new JLabel(new ImageIcon(ImageIO.read(new File("../pic/back2.jpg"))));
	        backG2.setBounds(0, 0, WINDOW_X, WINDOW_Y);
		}
        catch (IOException e) {
        	e.printStackTrace();
        }

		//タイトル画面へ
		changeWindow(winFlag);
	}

	//マウスクリック
	public void actionPerformed(ActionEvent e){
		int num = 0;
		String cmd = e.getActionCommand();

		//タイトル→登録，ログイン
		if(cmd.contains("Title:")) {
			ipAddress = text1.getText();
			port = text2.getText();

			//空欄
			if(ipAddress.equals("") || port.equals("")) {
				label4.setText("空欄");
			}
			//空欄じゃない
			else {
				//portが数字
				if(isNumber(port)) {
					portNum = Integer.parseInt(port);
					//接続
					connectServer(ipAddress, portNum);
					//接続できたら
					if(serverFlag) {
						if(cmd.contains("registration")){num = 1;}
						else {num = 6;}
						changeWindow(num);
					}
					else {
						changeWindow(0);
					}
				}
				else {
					label4.setText("数字を入力");
				}
			}
		}
		//登録→ログイン
		else if(cmd.equals("Registration:login")) {
			changeWindow(6);
		}
		//ログイン→登録
		else if(cmd.equals("Login:registration")) {
			changeWindow(1);

		}
		else if(cmd.equals("Game:surrender:true")) {
			cmd = "Game:surrender";
			sendMessage(cmd);
			changeWindow(22);
		}
		//対局→投了
		else if(cmd.equals("Game:surrender:false")) {
			changeWindow(16);
		}
		//対局→投了
		else if(cmd.equals("Game:surrender")) {
			changeWindow(19);
		}
		//エラー→タイトル
		else if(cmd.equals("Error:title")) {
			changeWindow(0);
		}

		//対局申請
		else if(cmd.equals("Matching:offer:")) {
			int i = 0;
			String input = text1.getText();
			String user = "";

			while(i < matchingUserNum){
				user = matchingUser[i].getName();
				if(user.equals(input)){
					break;
				}
				i++;
			}

			if(i == matchingUserNum || input.equals("") ){

			}
			else{
				opUser= new Player();
				String opData = user + "," +
					String.valueOf(matchingUser[i].getLevel()) + "," +
					String.valueOf(matchingUser[i].getWinPersentage());
				opUser.setOpponent(opData);
				sendMessage("Matching:offer:" + opData);
				changeWindow(12);
			}

		}

		//対局
		else if(cmd.contains("Game:")) {
			//自分のターン
			if(othello.getTurn() == myUser.getColor()) {
				sendMessage(cmd);
			}
			//投了
			//相手のターン
			else {
				label4.setText("相手のターン");
				repaint();
			}

		}


		//サーバへ
		else {
			//登録
			if(cmd.contains("Registration:")) {
				cmd = "Registration:" + text1.getText() + "," + text2.getText() + "," + text3.getText();
			}
			//ログイン
			else if(cmd.contains("Login:")) {
				cmd = "Login:" + text1.getText() + "," + text2.getText();
			}
			//ログアウト
			else if(cmd.contains("Mypage:logout")) {
				changeWindow(6);
			}
			//マッチングルームから退出
			else if(cmd.contains("Matching:exit")) {
				sendMessage("Matching:exit");
			}
			//対局申請受信履歴
			else if(cmd.contains("Matching:offerResult:")) {
				if(cmd.contains("ok")) {
					changeWindow(25);
				}
				else if(cmd.contains("fault")) {
					changeWindow(26);
				}
			}
			//終了
			if(cmd.equals("GameFinish:mypage")) {
				sendMessage(cmd);
				gameFlag = false;
				System.out.println("xx:" + gameFlag);
			}

			//サーバへ送信
			sendMessage(cmd);
		}
	}

	//サーバに接続
	public void connectServer(String ipAddress, int port){
		try {
			socket = new Socket(ipAddress, port); //サーバ接続
			serverFlag = true;
			out = new PrintWriter(socket.getOutputStream(), true); //send
			receiver = new Receiver(socket); //receive
			receiver.start();//スレッド起動
		}
		catch(Exception e) {
			System.err.println("サーバ接続エラー: " + e);
			serverFlag = false;
		}
	}

	//サーバに操作情報を送信
	public void sendMessage(String msg){
		out.println(msg);//送信データをバッファに書き出す
		out.flush();//送信データを送る
		System.out.println("send:    " + msg); //テスト標準出力
	}

	// データ受信用スレッド(内部クラス)
	class Receiver extends Thread {
		private InputStreamReader sisr; //受信データ用文字ストリーム
		private BufferedReader br; //文字ストリーム用のバッファ

		// 内部クラスReceiverのコンストラクタ
		Receiver (Socket socket){
			try{
				sisr = new InputStreamReader(socket.getInputStream()); //受信したバイトデータを文字ストリームに
				br = new BufferedReader(sisr);//文字ストリームをバッファリングする
			}
			catch (IOException e) {
				System.err.println("データ受信エラー: " + e);
			}
		}

		// 内部クラス Receiverのメソッド
		public void run(){
			try{
				while(true) {//データを受信し続ける
					String inputLine = br.readLine();//受信データを一行分読み込む
					if (inputLine != null){//データを受信したら
						receiveMessage(inputLine);//データ受信用メソッドを呼び出す
					}
					if(!gameFlag) { changeWindow(winFlag); }
				}
			}
			catch (IOException e){
				System.err.println("データ受信エラー: " + e);
				changeWindow(20);
			}
		}
	}

	//メッセージの受信
	public void receiveMessage(String msg){
		//受信したメッセージを標準出力
		System.out.println("receive: " + msg);

		//生きているか確認
		if(msg.contains("ping")) {
			sendMessage("pong");
		}

		//登録画面
		if(msg.contains("Registration:")) {
			if(msg.contains("returnTitle")) { winFlag = 0; }
			else if(msg.contains("blank")) {winFlag = 2;}
			else if(msg.contains("alreadyExists")) {winFlag = 3;}
			else if(msg.contains("notMatch")) {winFlag = 4;}
			else if(msg.contains("tooLong")) {winFlag = 27;}
			else if(msg.contains("success")){winFlag = 5; }
		}

		//ログイン画面
		if(msg.contains("Login:")) {
			if(msg.contains("blank")) {winFlag = 7;}
			else if(msg.contains("noUser")) {winFlag = 8;}
			else if(msg.contains("notMatch")) {winFlag = 9;}
			else if(msg.contains("success")) {
				winFlag = 10;
				String[] data = msg.split(":");
				userInfo = data[2].replaceAll("null", "");
				System.out.println(userInfo);
				myUser = new Player(userInfo);
			}
		}

		//マッチング画面
		if(msg.contains("Matching:")) {
			//入場
			if(msg.contains("enter:")) {
				String[] data1 = msg.split(":");

				//誰もいない
				if(data1.length == 2) {
					winFlag = 23;
				}
				else {
					winFlag = 11;
					String[] data2 = data1[2].split(","); //,で区切る
					matchingUserNum = data2.length / 3; //ユーザ数取得
					matchingUser = new Player[matchingUserNum];
					for(int i = 0; i < matchingUserNum; i++) {
						matchingUser[i] = new Player(data2[0 + i*3], data2[1 + i*3], data2[2 + i*3]);
					}
				}
			}
			//申請
			else if(msg.contains("offer:")) {
				winFlag = 12;
			}
			//申請結果
			else if(msg.contains("offerResult:")) {
				//ok
				if(msg.contains("ok")) {
					winFlag = 13;
				}
				//fault
				else if(msg.contains("fault")) {
					winFlag = 14;
				}
			}
			//申請受信
			else if(msg.contains("offerAccept:")) {
				winFlag = 15;
				String[] data = msg.split(":");
				opUser = new Player();
				opUser.setOpponent(data[2]);
			}
			//申請キャンセル
			else if(msg.contains("offerCancel")) {
				winFlag = 18;
			}
			//申請後ユーザがいない
			else if(msg.contains("offerNoUser")) {
				winFlag = 24;
			}
			//ゲーム画面へ
			else if(msg.contains("game")) {
				winFlag = 16;
				othello = new Othello(); //Othelloオブジェクト
				String[] data = msg.split(":");
				myUser.setColor(data[2]);
				opUser.setColor(!myUser.getColor());
			}
		}

		//対局画面
		if(msg.contains("Game:")){
			String[] data1 = msg.split(":");
			if(isNumber(data1[1])) {
				gameFlag = true;
				othello.putStone(othello.getTurn(), Integer.parseInt(data1[1]));
				updateDisp();
			}
			else if(data1[1].equals("surrenderAccept")) {
				winFlag = 21;
				gameFlag = false;
			}
		}
	}

	//画面遷移
	public void changeWindow(int winFlag) {
		//デバッグ
		System.out.println("flag:" + winFlag);

		//全消去
		c.removeAll();

		//クローズ処理
		if(winFlag < 10 || winFlag == 27) {
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		else {
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		}


		//画面遷移
		switch(winFlag) {
		//タイトル
		case 0:
			//タイトルロゴ
			label1 = new JLabel("OTHELLO GAME");
			x = culcCenter(WINDOW_X, 420);
			y = (int)(WINDOW_Y / 4);
			label1.setBounds(x, y, 420, 100);
			label1.setFont( new Font(Font.DIALOG, Font.BOLD | Font.ITALIC, 50));
			label1.setBorder(border);
			label1.setForeground(Color.WHITE);
			label1.setOpaque(true);
			label1.setHorizontalAlignment(JLabel.CENTER);
			label1.setBackground(Color.BLACK);
			add(label1);

			//ip
			//ラベル
			label2 = new JLabel("IPアドレス");
			label2.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			x = culcCenter(WINDOW_X, 500);
			label2.setBounds(x, 270, 200, 35);
			label2.setForeground(Color.WHITE);
			label2.setOpaque(true);
			label2.setHorizontalAlignment(JLabel.CENTER);
			label2.setBackground(Color.BLACK);
			add(label2);
			//テキスト
			text1 = new JTextField("");
		    text1.setFont(new Font(Font.DIALOG, Font.PLAIN, 40));
		    text1.setBounds(x+200, 270, 300, 35);
		    add(text1);

			//port
			//ラベル
			label3 = new JLabel("ポート番号");
		    label3.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
		    label3.setBounds(x, 320, 200, 35);
		    label3.setForeground(Color.WHITE);
		    label3.setOpaque(true);
		    label3.setHorizontalAlignment(JLabel.CENTER);
			label3.setBackground(Color.BLACK);
			add(label3);
			//テキスト
			text2 = new JTextField("");
		    text2.setFont(new Font(Font.DIALOG, Font.PLAIN, 40));
		    text2.setBounds(x+200, 320, 300, 35);
			add(text2);

			//登録ボタン
			button1 = new JButton("登録");
			x = 200;
			button1.setFont(new Font(Font.DIALOG, Font.PLAIN, 35));
			button1.setBounds(x, 400, 200, 50);
			button1.addActionListener(this);
			button1.setActionCommand("Title:registration");
			button1.setForeground(Color.WHITE);
			button1.setBackground(Color.BLACK);
			add(button1);

			//ログインボタン
			button2 = new JButton("ログイン");
			button2.setFont(new Font(Font.DIALOG, Font.PLAIN, 35));
			button2.setBounds(550, 400, 200, 50);
			button2.addActionListener(this);
			button2.setActionCommand("Title:login");
			button2.setForeground(Color.WHITE);
			button2.setBackground(Color.BLACK);
			add(button2);

			//エラー
			label4 = new JLabel();
			if(!serverFlag) {
				label4.setText("入力が間違っています。");
			}
		    label4.setForeground(Color.RED);
		    label4.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
		    x = 350;
		    label4.setBounds(x, 90, 500, 35);
		    add(label4);

			//背景
			add(backG1);

			break;

		//登録
		case 1:
			label1 = new JLabel(" 登録");
			label1.setFont(new Font("ＭＳ ゴシック", Font.PLAIN, 50));
		    x = culcCenter(WINDOW_X, 200);
		    label1.setForeground(Color.WHITE);
		    label1.setBounds(x, 25, 200, 50);
			add(label1);

			//説明
			label2 = new JLabel("登録したいユーザ名、パスワードを入力してください");
		    label2.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
		    x = culcCenter(WINDOW_X, 800);
		    label2.setBounds(x, 120, 800, 40);
		    label2.setForeground(Color.WHITE);
		    add(label2);

			//ユーザ名
		    //ラベル
		    label3 = new JLabel("<html><body>　ユーザ名<br />(半角8文字以内)</body></html>");
		    label3.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
		    x = culcCenter(WINDOW_X, 500);
		    label3.setBounds(x, 170, 250, 100);
		    label3.setForeground(Color.WHITE);
		    add(label3);
		    //テキスト
		    text1 = new JTextField("");
		    text1.setFont(new Font(Font.DIALOG, Font.PLAIN, 40));
		    text1.setBounds(x+250, 205, 250, 35);
			add(text1);

			//password1
			//ラベル
			label4 = new JLabel("パスワード");
		    label4.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
		    label4.setBounds(x, 265, 250, 35);
		    label4.setForeground(Color.WHITE);
		    add(label4);
			//テキスト
		    text2 = new JTextField("");
		    text2.setFont(new Font(Font.DIALOG, Font.PLAIN, 40));
		    text2.setBounds(x+250, 265, 250, 35);
		    add(text2);

		    //password2
			//ラベル
		    label5 = new JLabel("<html><body>パスワード<br />　(確認用)</body></html>");
		    label5.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
		    label5.setBounds(x, 300, 250, 95);
		    label5.setForeground(Color.WHITE);
		    add(label5);
			//テキスト
		    text3 = new JTextField("");
		    text3.setFont(new Font(Font.DIALOG, Font.PLAIN, 40));
		    text3.setBounds(x+250, 320, 250, 35);
			add(text3);

			//登録ボタン
			button1 = new JButton("登録");
			button1.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
		    button1.setBounds(400, 400, 200, 60);
			button1.addActionListener(this);
			button1.setActionCommand("Registration:");
			button1.setBackground(Color.WHITE);
			add(button1);

			//ログイン画面へ
			button2 = new JButton("ログイン画面へ");
			button2.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
			y = WINDOW_Y - 160;
			button2.setBounds(0, y, 200, 60);
			button2.addActionListener(this);
			button2.setActionCommand("Registration:login");
			button2.setBackground(Color.WHITE);
			add(button2);

			//エラー
			label6 = new JLabel();
		    label6.setForeground(Color.RED);
		    label6.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
		    x = culcCenter(WINDOW_X, 500);
		    label6.setBounds(x, 90, 500, 35);
		    add(label6);

			//背景
			add(backG2);

			break;

		//登録(エラー1)
		case 2:
			//エラー
			label6.setText("未記入の欄があります。");
			label6.setBounds(320, 80, 500, 35);
			add(label6);

		    add(label1);
		    add(label2);
		    add(label3);
		    add(label4);
		    add(label5);
		    add(text1);
		    add(text2);
		    add(text3);
		    add(button1);
		    add(button2);
			add(backG2);

			break;

		//登録(エラー2)
		case 3:
			//エラー
			label6.setText("既に使われているユーザ名です。");
			label6.setBounds(280, 80, 500, 35);
		    add(label6);

		    add(label1);
		    add(label2);
		    add(label3);
		    add(label4);
		    add(label5);
		    add(text1);
		    add(text2);
		    add(text3);
		    add(button1);
		    add(button2);
			add(backG2);

			break;

		//登録(エラー3)
		case 4:
			//エラー
			label6.setText("パスワードが一致しません。");
			label6.setBounds(300, 80, 500, 35);
		    add(label6);

		    add(label1);
		    add(label2);
		    add(label3);
		    add(label4);
		    add(label5);
		    add(text1);
		    add(text2);
		    add(text3);
		    add(button1);
		    add(button2);
			add(backG2);

			break;

		//登録完了
		case 5:
			//表示
			label1 = new JLabel("登録完了！");
			x = culcCenter(WINDOW_X, 859)+250;
			y = WINDOW_Y / 2 - 100;
			label1.setBounds(x, y, 859, 70);
			label1.setFont(new Font("ＭＳゴシック", Font.PLAIN, 70));
			label1.setForeground(Color.WHITE);
			add(label1);

			//ログイン画面へ
			button1 = new JButton("ログインへ");
			button1.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
			y = WINDOW_Y - 160;
			button1.setBounds(0, y, 150, 60);
			button1.addActionListener(this);
			button1.setActionCommand("Registration:login");
			button1.setBackground(Color.WHITE);
			add(button1);

			//背景
			add(backG2);

			break;

		//ログイン
		case 6:
			 //タイトル
			label1 = new JLabel("ログイン");
		    label1.setFont(new Font("ＭＳ ゴシック", Font.PLAIN, 50));
		    x = culcCenter(WINDOW_X, 200);
		    label1.setBounds(x, 25, 200, 50);
		    label1.setForeground(Color.WHITE);
			add(label1);

			//説明
			label2 = new JLabel("ユーザ名、パスワードを入力してください");
		    label2.setFont(new Font(Font.DIALOG, Font.PLAIN, 35));
		    x = culcCenter(WINDOW_X,850) + 80;
		    label2.setBounds(x, 180, 850, 45);
		    label2.setForeground(Color.WHITE);
		    add(label2);

			//ユーザ名
		    label3 = new JLabel("ユーザ名");
		    label3.setFont(new Font(Font.DIALOG, Font.PLAIN, 35));
		    label3.setBounds(265, 265, 1000, 35);
		    label3.setForeground(Color.WHITE);
		    add(label3);

			//テキスト
		    text1 = new JTextField("");
		    text1.setFont(new Font(Font.DIALOG, Font.PLAIN, 25));
		    text1.setBounds(500, 265, 300, 35);
			add(text1);

			//password1
			//ラベル
			label4 = new JLabel("パスワード");
		    label4.setFont(new Font(Font.DIALOG, Font.PLAIN, 35));
		    label4.setBounds(265, 315, 1000, 35);
		    label4.setForeground(Color.WHITE);
		    add(label4);
			//テキスト
		    text2 = new JTextField("");
		    text2.setFont(new Font(Font.DIALOG, Font.PLAIN, 25));
		    text2.setBounds(500, 315, 300, 35);
		    add(text2);

			//ボタン
			button1 = new JButton("ログイン");
			button1.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
		    button1.setBounds(400, 400, 200, 60);
			button1.addActionListener(this);
			button1.setActionCommand("Login:");
			button1.setBackground(Color.WHITE);
			add(button1);

			//登録画面へ
			button2 = new JButton("登録画面へ");
			button2.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
			y = WINDOW_Y - 160;
			button2.setBounds(0, y, 150, 60);
			button2.addActionListener(this);
			button2.setActionCommand("Login:registration");
			button2.setBackground(Color.WHITE);
			add(button2);

			//エラー
			label5 = new JLabel();
		    label5.setForeground(Color.RED);
		    label5.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
		    x = culcCenter(WINDOW_X, 500);
		    label5.setBounds(x, 90, 500, 35);
		    add(label5);


			//背景
			add(backG2);

			break;

		//ログイン(エラー1)
		case 7:
			//エラー
			label5.setText("未記入の欄があります。");
			label5.setBounds(340,110, 500, 35);
		    add(label5);

			add(label1);
		    add(label2);
		    add(label3);
		    add(label4);
			add(text1);
		    add(text2);
			add(button1);
			add(button2);
			add(backG2);

			break;

		//ログイン(エラー2)
		case 8:
			//エラー
			label5.setText("ユーザ名が存在しません。");
			 label5.setBounds(320,110, 500, 35);
		    add(label5);

			add(label1);
		    add(label2);
		    add(label3);
		    add(label4);
			add(text1);
		    add(text2);
			add(button1);
			add(button2);
			add(backG2);

			break;

		//ログイン(エラー3)
		case 9:
		    //エラー
			label5.setText("パスワードが一致しません。");
			label5.setBounds(310,110, 500, 35);
		    add(label5);

			add(label1);
		    add(label2);
		    add(label3);
		    add(label4);
			add(text1);
		    add(text2);
			add(button1);
			add(button2);
			add(backG2);

			break;

		//マイページ
		case 10:
			//タイトル
			String str = rightPad(myUser.getName(), 8);
			label1 = new JLabel(str + "のマイページ");
			label1.setFont(new Font("ＭＳ ゴシック", Font.PLAIN, 50));
			x = culcCenter(WINDOW_X, 500);
		    label1.setBounds(x, 20, 500, 100);
		    label1.setForeground(Color.WHITE);
		     add(label1);
			//成績1
			label2 = new JLabel("レベル:" + myUser.getLevel() + "  勝率:" + myUser.getWinPersentage() + "%");
			x = culcCenter(WINDOW_X, 350) + 20;
			label2.setBounds(x, 120, 350, 30);
			label2.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			label2.setForeground(Color.WHITE);
			add(label2);

			//ラベル1
			String a[]=myUser.getResult().split(" ",-1);
			label3 = new JLabel("総試合数:" + a[0]+ " 勝:" + a[2] + " 負:" + a[4] + " 分:" + a[6]+ "投了:" + a[8]);
			x = culcCenter(WINDOW_X, 550) + 20;
			label3.setBounds(x, 150, 550, 50);
			label3.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			label3.setForeground(Color.WHITE);
			add(label3);

			//ラベル2
			label5 = new JLabel("対局履歴");
			x = culcCenter(WINDOW_X, 250);
			label5.setBounds(x, 210, 250, 25);
			label5.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			label5.setForeground(Color.BLACK);
			label5.setOpaque(true);
			label5.setHorizontalAlignment(JLabel.CENTER);
			label5.setBackground(Color.WHITE);
			add(label5);

			label4=new JLabel("対局相手");
		    label4.setFont(new Font("Font.DIALOG", Font.PLAIN, 20));
		    x  = culcCenter(WINDOW_X, 500);
		    y  = 245;
		    label4.setBounds(x,y,230,25);
		    label4.setForeground(Color.BLACK);
		    label4.setOpaque(true);
			label4.setHorizontalAlignment(JLabel.CENTER);
			label4.setBackground(Color.WHITE);
		    add(label4);

		    label6=new JLabel("結果");
		    label6.setFont(new Font("Font.DIALOG", Font.PLAIN, 20));
		    x += 230 + 40;
		    y = 245;
		    label6.setBounds(x,y,230,25);
		    label6.setForeground(Color.BLACK);
		    label6.setOpaque(true);
			label6.setHorizontalAlignment(JLabel.CENTER);
			label6.setBackground(Color.WHITE);
		    add(label6);

			//対局履歴


			 disparea=new JTextArea();
			 disparea.setFont(new Font("Font.DIALOG", Font.PLAIN, 25));
			 sb = new StringBuilder();

			y = 245;
			System.out.println(userInfo);
			String history = myUser.getStrHistory(userInfo);
			num = myUser.getHistoryNum(history);
			arrayLabel = new JLabel[num];
			for(int i = 0; i < num; i++) {
		/*		String str4 = myUser.getHistory(history, i);
				String[] str5 = str4.split(",");
				arrayLabel[i] = new JLabel(rightPad(str5[0], 38) + str5[1]);
				x = 300;
				y += 30;
				arrayLabel[i].setBounds(x, y, 500, 30);
				arrayLabel[i].setFont(new Font(Font.DIALOG, Font.PLAIN, 25));
				arrayLabel[i].setForeground(Color.WHITE);
				add(arrayLabel[i]);  */


				String b[]=myUser.getHistory(history, i).split(",",0);
				String gg=new String(b[0]+"                                       　"+b[1]);
				sb.append(gg+"\n");


			}

			disparea.setText(new String(sb));
		    scrollpane1= new JScrollPane(disparea);
		    scrollpane1.setPreferredSize(new Dimension(100, 4000));
		    scrollpane1.setBounds(230, 275, 480, 150);
		    scrollpane1.setViewportView(disparea);
		    add(scrollpane1);

			//ボタン
			button1 = new JButton("マッチングルームへ");
			button1.setFont(new Font("Font.DIALOG", Font.PLAIN, 25));
			x = 600;
			button1.setBounds(x, 440, 330, 50);
			button1.addActionListener(this);
			button1.setActionCommand("Mypage:matching");
			button1.setBackground(Color.WHITE);
			add(button1);

			//ログアウト
			button2 = new JButton("ログアウト");
			button2.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
			x = 0;
			y = 440;
			button2.setBounds(0, y, 150, 50);
			button2.addActionListener(this);
			button2.setActionCommand("Mypage:logout");
			button2.setBackground(Color.WHITE);
			add(button2);


			//背景
			add(backG2);

			break;

		//マッチングルーム
		case 11:

			//タイトル
			label1 = new JLabel("マッチングルーム");
			label1.setFont(new Font("ＭＳ ゴシック", Font.PLAIN, 50));
			x = 300;
			label1.setBounds(x, 10, 400, 100);
			label1.setForeground(Color.WHITE);
			add(label1);

			//プロフィール
			String str1 = rightPad(myUser.getName(), 8);
			label2 = new JLabel("名前:" + str1 + "レベル:" + myUser.getLevel() + "  勝率:" + myUser.getWinPersentage() + "%");
			x = 300;
			label2.setBounds(x, 100, 500, 50);
			label2.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			label2.setForeground(Color.WHITE);
			add(label2);

			//相手
			y = 120;

			disparea=new JTextArea();
			 disparea.setFont(new Font("Font.DIALOG", Font.PLAIN, 25));
			 sb = new StringBuilder();


			arrayLabel = new JLabel[matchingUserNum];
			arrayButton = new JButton[matchingUserNum];
			for(int i = 0; i < matchingUserNum && i<5; i++) {
				String str2 = rightPad(matchingUser[i].getName(), 8);
				String str3 = "名前：" + str2 +
								" レベル：" + matchingUser[i].getLevel() +
								"  勝率：" + matchingUser[i].getWinPersentage() + "%";
				sb.append(str3+"\n");
				x = culcCenter(WINDOW_X, 600);
				y += 40;
			}

			disparea.setText(new String(sb));
		    scrollpane1= new JScrollPane(disparea);
		    scrollpane1.setPreferredSize(new Dimension(100, 4000));
		    scrollpane1.setBounds(100, 165, 500, 200);
		    scrollpane1.setViewportView(disparea);
		    add(scrollpane1);

			//マイページへ
			button1 = new JButton("マイページへ");
			button1.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			y = WINDOW_Y - 160;
			button1.setBounds(0, y, 250, 40);
			button1.addActionListener(this);
			button1.setActionCommand("Matching:exit");
			button1.setBackground(Color.WHITE);
			add(button1);

			//更新
			button2 = new JButton("更新");
			button2.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			x = culcCenter(WINDOW_X, 150);
			y = WINDOW_Y - 160;
			button2.setBounds(x, y, 150, 40);
			button2.addActionListener(this);
			button2.setActionCommand("Matching:update");
			button2.setBackground(Color.WHITE);

			//ユーザ名
			label3 = new JLabel("対局したいユーザ名を入力");
			label3.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
			x = 650;
			label3.setBounds(x, 165, 250, 30);
			label3.setForeground(Color.WHITE);
			add(label3);

			//テキスト
		    text1 = new JTextField("");
		    text1.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
		    text1.setBounds(x, 220, 250, 35);
			add(text1);

			//対局申請送信
			button3 = new JButton("対局申請送信");
			button3.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			button3.setBounds(x, 300, 250, 50);
			button3.addActionListener(this);
			button3.setActionCommand("Matching:offer:");
			button3.setBackground(Color.WHITE);
			add(button3);
			add(button2);

			//背景
			add(backG2);

			break;

		//対局申請送信
		case 12:
			//ラベル
			label1 = new JLabel("対局申請送信中...");
			x = culcCenter(WINDOW_X, 400);
			y = WINDOW_Y / 2 - 100;
			label1.setBounds(x, y, 400, 60);
			label1.setFont(new Font("Font.DIALOG", Font.PLAIN, 50));
			label1.setForeground(Color.WHITE);
			add(label1);

			//マイページへ戻る
			button1 = new JButton("マイページへ戻る");
			button1.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
			x = 0;
			y = WINDOW_Y - 160;
			button1.setBounds(x, y, 150, 60);
			button1.addActionListener(this);
			button1.setActionCommand("Matching:offerCancel");
			button1.setBackground(Color.WHITE);
			add(button1);

			//背景
			add(backG2);

			break;

		//対局申請結果ok
		case 13:
			//ラベル
			label1 = new JLabel("対局申請が許可されました");
			x = culcCenter(WINDOW_X, 600);
			y = WINDOW_Y / 2 - 100;
			label1.setBounds(x, y, 600, 60);
			label1.setFont(new Font("ＭＳ ゴシック", Font.PLAIN, 50));
			label1.setForeground(Color.WHITE);
			add(label1);

			//対局へ
			button1 = new JButton("対局へ");
			button1.setFont(new Font("Font.DIALOG", Font.PLAIN, 30));
			x = culcCenter(WINDOW_X, 150);
			y += 60 + 60;
			button1.setBounds(x, y, 150, 50);
			button1.addActionListener(this);
			button1.setActionCommand("Matching:game");
			button1.setBackground(Color.WHITE);
			add(button1);

			//背景
			add(backG2);

			break;

		//対局申請結果faullt
		case 14:
			//ラベル
			label1 = new JLabel("対局申請が拒否されました");
			x = culcCenter(WINDOW_X, 600);
			y = WINDOW_Y / 2 - 100;
			label1.setBounds(x, y, 600, 60);
			label1.setFont(new Font("ＭＳ ゴシック", Font.PLAIN, 50));
			label1.setForeground(Color.WHITE);
			add(label1);

			//マッチングルームへ
			button1 = new JButton("マッチングルームへ");
			button1.setFont(new Font("Font.DIALOG", Font.PLAIN, 25));
			x = culcCenter(WINDOW_X, 220);
			y += 60 + 30;
			button1.setBounds(x, y, 290, 50);
			button1.addActionListener(this);
			button1.setActionCommand("Mypage:matching");
			button1.setBackground(Color.WHITE);
			add(button1);

			//背景
			add(backG2);

			break;

		//対局申請受信
		case 15:
			//ラベル
			label1 = new JLabel("対局申請を受信しました");
			x = culcCenter(WINDOW_X, 400);
			y = WINDOW_Y / 2 - 100;
			label1.setBounds(x, y, 400, 60);
			label1.setFont(new Font(Font.DIALOG, Font.PLAIN, 40));
			label1.setForeground(Color.WHITE);
			add(label1);

			//ラベル2
			String c[]= opUser.getOpponent().split(" ",0);
			label2 = new JLabel("ユーザ名"+c[0]+" レベル"+c[2]+" 勝率"+c[4] + "%");
			x = culcCenter(WINDOW_X, 600);
			y += 60 + 30;
			label2.setBounds(x, y, 600, 60);
			label2.setFont(new Font(Font.DIALOG, Font.PLAIN, 40));
			label2.setForeground(Color.WHITE);
			add(label2);


			//許可or拒否
			button1 = new JButton("許可");
			button2 = new JButton("拒否");
			x = culcCenter(WINDOW_X, 150 * 2 + 10);
			y += 70;
			button1.setBounds(x, y, 150, 50);
			button2.setBounds(x + 150 + 10, y, 150, 50);
			button1.addActionListener(this);
			button2.addActionListener(this);
			button1.setActionCommand("Matching:offerResult:ok");
			button2.setActionCommand("Matching:offerResult:fault");
			button1.setBackground(Color.WHITE);
			button2.setBackground(Color.WHITE);
			add(button1);
			add(button2);

			//背景
			add(backG2);

			break;

		//対局画面
		case 16:
			//オセロ盤の生成
			arrayButton = new JButton[row * row];//ボタンの配列を作成
			num = 0;
			//x座標：j，y座標：i
			for(int i = 1; i <= row; i++){
				for(int j = 1; j <= row; j++){
					int gridsState = othello.getGridsState(j, i);
					if(gridsState != Othello.S){ //番兵じゃないなら
						//盤面状態に応じたアイコンを設定
						if(gridsState == Othello.B){ arrayButton[num] = new JButton(blackIcon);}
						else if(gridsState == Othello.W){ arrayButton[num] = new JButton(whiteIcon);}
						else if(gridsState == Othello.N){ arrayButton[num] = new JButton(boardIcon);}
						add(arrayButton[num]);//ボタンの配列をペインに貼り付け

						// ボタンを配置する
						x = (num % row) * 45 + 60;
						y = (int) (num / row) * 45 + 60;
						arrayButton[num].setBounds(x, y, 45, 45);//ボタンの大きさと位置を設定する．
						arrayButton[num].addActionListener(this);//マウス操作を認識できるようにする
						arrayButton[num].setActionCommand("Game:" + Integer.toString(othello.changeDemension(j, i)));//ボタンを識別するための名前(番号)を付加する
						num += 1;
					}
				}
			}

			//石の数セット
			myUser.setStone(othello.getStoneNum(myUser.getColor()));
			opUser.setStone(othello.getStoneNum(opUser.getColor()));

			if(!myUser.getColor()) {
				myStr = "●" + rightPad(myUser.getName(), 8) + "  石の数:" + String.valueOf(myUser.getStone());
				opStr = "○" + rightPad(opUser.getName(), 8) + "  石の数:" + String.valueOf(opUser.getStone());
			}
			else {
				myStr = "○" + rightPad(myUser.getName(), 8) + "  石の数:" + String.valueOf(myUser.getStone());
				opStr = "●" + rightPad(opUser.getName(), 8) + "  石の数:" + String.valueOf(opUser.getStone());
			}

			//相手の名前
			label1 = new JLabel(opStr);
			label1.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			x = 45 * row + 110;
			y = 60;
			label1.setBounds(x, y, 300, 60);
			label1.setOpaque(true);
			label1.setHorizontalAlignment(JLabel.CENTER);
			label1.setForeground(Color.WHITE);
			label1.setBackground(Color.BLACK);
			add(label1);

			//自分の名前
			label2 = new JLabel(myStr);
			label2.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			label2.setBounds(x, 350, 300, 60);
			label2.setOpaque(true);
			label2.setHorizontalAlignment(JLabel.CENTER);
			label2.setForeground(Color.WHITE);
			label2.setBackground(Color.BLACK);
			add(label2);

			//色表示用ラベル
			if(othello.getTurn()) {
				label3 = new JLabel("現在のターン: ●");//手番情報を表示するためのラベルを作成
			}
			else {
				label3 = new JLabel("現在のターン: ○");//手番情報を表示するためのラベルを作成
			}
			label3.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			y = 45 * 2 + 80;
			label3.setBounds(x, y, 350, 60);//境界を設定
			label3.setBorder(border);
			label3.setOpaque(true);
			label3.setHorizontalAlignment(JLabel.CENTER);
			label3.setBackground(Color.WHITE);
			add(label3);//手番情報ラベルをペインに貼り付け

			//メッセ－ジラベル
			label4 = new JLabel();
			label4.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
			y = 45 * 4 + 80;
			label4.setBounds(x, y, 350, 60);//境界を設定
			label4.setBorder(border);
			label4.setOpaque(true);
			label4.setHorizontalAlignment(JLabel.CENTER);
			label4.setBackground(Color.WHITE);
			add(label4);//手番情報ラベルをペインに貼り付け

			//投了ボタン
			button1 = new JButton("投了");
			x += 300 + 20;
			button1.setBounds(x, 350, 100, 60);
			button1.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			button1.addActionListener(this);
			button1.setActionCommand("Game:surrender");
			button1.setBackground(Color.BLUE);
			button1.setForeground(Color.WHITE);
			add(button1);

			//背景
			add(backG2);

			break;

		case 17:
			//オセロ盤の生成
			arrayLabel = new JLabel[row * row];
			num = 0;
			for(int i = 1; i <= row; i++){
				for(int j = 1; j <= row; j++){
					int gridsState = othello.getGridsState(j, i);
					if(gridsState != Othello.S){
						if(gridsState == Othello.B){ arrayLabel[num] = new JLabel(blackIcon);}
						else if(gridsState == Othello.W){ arrayLabel[num] = new JLabel(whiteIcon);}
						else if(gridsState == Othello.N){ arrayLabel[num] = new JLabel(boardIcon);}
						add(arrayLabel[num]);
						x = (num % row) * 45 + 60;
						y = (int) (num / row) * 45 + 60;
						arrayLabel[num].setBounds(x, y, 45, 45);
						num += 1;
					}
				}
			}

			//ラベル
			label1 = new JLabel("対局終了");
			label1.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			x = 60 + 45 * row + 50;
			y = 60;
			label1.setBounds(x, y, 360, 90);
			label1.setForeground(Color.WHITE);
			add(label1);

			//石の数セット
			myUser.setStone(othello.getStoneNum(myUser.getColor()));
			opUser.setStone(othello.getStoneNum(opUser.getColor()));

			//メッセ－ジラベル
			label2 = new JLabel();
			//表示
			if(myUser.getStone() > opUser.getStone()) {
				label2.setText("you win");
				myUser.setGameResult(String.valueOf(1));
			}
			else if(myUser.getStone() < opUser.getStone()) {
				label2.setText("you lose");
				myUser.setGameResult(String.valueOf(2));
			}
			else {
				label2.setText("Draw");
				myUser.setGameResult(String.valueOf(3));
			}
			label2.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			y = 45 * 4 + 80;
			label2.setBounds(x, y, 250, 60);
			label2.setForeground(Color.WHITE);
			add(label2);

			//石の数ラベル
			label3 = new JLabel(rightPad(myUser.getName(), 8) + "の石の数:" + String.valueOf(myUser.getStone()));
			label4 = new JLabel(rightPad(opUser.getName(), 8) + "の石の数:" + String.valueOf(opUser.getStone()));
			label3.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			label4.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			label3.setBounds(x, 330, 250, 40);
			label4.setBounds(x, 380, 250, 40);
			label3.setForeground(Color.WHITE);
			label4.setForeground(Color.WHITE);
			add(label3);
			add(label4);

			//マイページへ
			button1 = new JButton("マイページへ");
			button1.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			x = 550;
			y = 200;
			button1.setBounds(x, y, 250, 50);
			button1.addActionListener(this);
			button1.setActionCommand("GameFinish:mypage");
			button1.setBackground(Color.WHITE);
			add(button1);

			//対局記録送信
			myUser.setExp(myUser.getGameResult(), opUser.getLevel());
			gameResult = "GameFinish:" + myUser.getName() + "," + opUser.getName() + "," + myUser.getGameResult() + "," + String.valueOf(myUser.getExp());
			sendMessage(gameResult);

			//exp
			label5 = new JLabel("獲得経験値:" + String.valueOf(myUser.culcAddExp(myUser.getGameResult(), opUser.getLevel())));
			label5.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			label5.setBounds(500, 430, 250, 40);
			label5.setForeground(Color.WHITE);
			add(label5);

			//背景
			add(backG2);

			break;

		//申請キャンセル
		case 18:
			//ラベル
			label1 = new JLabel("対局申請がキャンセルされました");
			x = culcCenter(WINDOW_X, 500);
			y = WINDOW_Y / 2 - 100;
			label1.setBounds(x, y, 500, 60);
			label1.setFont(new Font(Font.DIALOG, Font.PLAIN, 40));
			label1.setForeground(Color.WHITE);
			add(label1);

			//戻るボタン
			button1 = new JButton("マッチングルームへ");
			x = culcCenter(WINDOW_X, 300);
			y += 100;
			button1.setBounds(x, y, 300, 90);
			button1.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			button1.addActionListener(this);
			button1.setActionCommand("Mypage:matching");
			button1.setBackground(Color.WHITE);
			add(button1);

			//背景
			add(backG2);

			break;

		//投了画面
		case 19:
			//ラベル
			label1 = new JLabel("投了しますか？");
			x = culcCenter(WINDOW_X, 400);
			y = WINDOW_Y / 2 - 100;
			label1.setBounds(x, y, 400, 60);
			label1.setFont(new Font(Font.DIALOG, Font.PLAIN, 40));
			label1.setForeground(Color.WHITE);
			add(label1);

			//投了or戻る
			button1 = new JButton("投了");
			button2 = new JButton("戻る");
			x = culcCenter(WINDOW_X, 150 * 2 + 10);
			y += 60;
			button1.setBounds(x, y, 150, 50);
			button2.setBounds(x + 160, y, 150, 50);
			button1.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			button2.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			button1.addActionListener(this);
			button2.addActionListener(this);
			button1.setActionCommand("Game:surrender:true");
			button2.setActionCommand("Game:surrender:false");
			button1.setBackground(Color.WHITE);
			button2.setBackground(Color.WHITE);
			add(button1);
			add(button2);

			//背景
			add(backG2);

			break;

		//エラー画面
		case 20:
			label1 = new JLabel("エラー");
			x = 400;
			y = WINDOW_Y / 2 - 100;
			label1.setBounds(x, y, 400, 60);
			label1.setFont(new Font(Font.DIALOG, Font.PLAIN, 60));
			label1.setForeground(Color.WHITE);
			add(label1);

			//投了or戻る
			button1 = new JButton("タイトルへ");
			button1.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			x = 390;
			y += 100;
			button1.setBounds(x, y, 200, 50);
			button1.addActionListener(this);
			button1.setActionCommand("Error:title");
			button1.setBackground(Color.WHITE);
			add(button1);


			//背景
			add(backG2);

			break;

		//投了受信
		case 21:
			//オセロ盤の生成
			arrayLabel = new JLabel[row * row];
			num = 0;
			for(int i = 1; i <= row; i++){
				for(int j = 1; j <= row; j++){
					int gridsState = othello.getGridsState(j, i);
					if(gridsState != Othello.S){
						if(gridsState == Othello.B){ arrayLabel[num] = new JLabel(blackIcon);}
						else if(gridsState == Othello.W){ arrayLabel[num] = new JLabel(whiteIcon);}
						else if(gridsState == Othello.N){ arrayLabel[num] = new JLabel(boardIcon);}
						add(arrayLabel[num]);
						x = (num % row) * 45 + 60;
						y = (int) (num / row) * 45 + 60;
						arrayLabel[num].setBounds(x, y, 45, 45);
						num += 1;
					}
				}
			}

			//ラベル
			label1 = new JLabel("対局終了, 投了されました。");
			label1.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			x = 500;
			y = 60;
			label1.setBounds(x, y, 380, 90);
			label1.setForeground(Color.WHITE);
			add(label1);

			//対戦結果を保持
			myUser.setGameResult(String.valueOf(1));

			//石の数ラベル
			label3 = new JLabel(rightPad(myUser.getName(), 8) + "の石の数:" + String.valueOf(myUser.getStone()));
			label4 = new JLabel(rightPad(opUser.getName(), 8) + "の石の数:" + String.valueOf(opUser.getStone()));
			label3.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			label4.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			label3.setBounds(x, 330, 250, 40);
			label4.setBounds(x, 380, 250, 40);
			label3.setForeground(Color.WHITE);
			label4.setForeground(Color.WHITE);
			add(label3);
			add(label4);

			//マイページへ
			button1 = new JButton("マイページへ");
			button1.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			x = 550;
			y = 200;
			button1.setBounds(x, y, 250, 50);
			button1.addActionListener(this);
			button1.setActionCommand("GameFinish:mypage");
			button1.setBackground(Color.WHITE);
			add(button1);

			//対局記録送信
			myUser.setExp(myUser.getGameResult(), opUser.getLevel());
			gameResult = "GameFinish:" + myUser.getName() + "," + opUser.getName() + "," + myUser.getGameResult() + "," + String.valueOf(myUser.getExp());
			sendMessage(gameResult);

			//exp
			label5 = new JLabel("獲得経験値:" + String.valueOf(myUser.culcAddExp(myUser.getGameResult(), opUser.getLevel())));
			label5.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			label5.setBounds(500, 430, 250, 40);
			label5.setForeground(Color.WHITE);
			add(label5);

			//背景
			add(backG2);

			break;

		//投了送信
		case 22:
			//オセロ盤の生成
			arrayLabel = new JLabel[row * row];
			num = 0;
			for(int i = 1; i <= row; i++){
				for(int j = 1; j <= row; j++){
					int gridsState = othello.getGridsState(j, i);
					if(gridsState != Othello.S){
						if(gridsState == Othello.B){ arrayLabel[num] = new JLabel(blackIcon);}
						else if(gridsState == Othello.W){ arrayLabel[num] = new JLabel(whiteIcon);}
						else if(gridsState == Othello.N){ arrayLabel[num] = new JLabel(boardIcon);}
						add(arrayLabel[num]);
						x = (num % row) * 45 + 60;
						y = (int) (num / row) * 45 + 60;
						arrayLabel[num].setBounds(x, y, 45, 45);
						num += 1;
					}
				}
			}

			//ラベル
			label1 = new JLabel("対局終了, 投了しました。");
			label1.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			x = 500;
			y = 60;
			label1.setBounds(x, y, 380, 90);
			label1.setForeground(Color.WHITE);
			add(label1);

			//結果セット
			myUser.setGameResult(String.valueOf(4));

			//石の数ラベル
			label3 = new JLabel(rightPad(myUser.getName(), 8) + "の石の数:" + String.valueOf(myUser.getStone()));
			label4 = new JLabel(rightPad(opUser.getName(), 8) + "の石の数:" + String.valueOf(opUser.getStone()));
			label3.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			label4.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			label3.setBounds(x, 330, 250, 40);
			label4.setBounds(x, 380, 250, 40);
			label3.setForeground(Color.WHITE);
			label4.setForeground(Color.WHITE);
			add(label3);
			add(label4);

			//マイページへ
			button1 = new JButton("マイページへ");
			button1.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			x = 550;
			y = 200;
			button1.setBounds(x, y, 250, 50);
			button1.addActionListener(this);
			button1.setActionCommand("GameFinish:mypage");
			button1.setBackground(Color.WHITE);
			add(button1);

			//対局記録送信
			myUser.setExp(myUser.getGameResult(), opUser.getLevel());
			gameResult = "GameFinish:" + myUser.getName() + "," + opUser.getName() + "," + myUser.getGameResult() + "," + String.valueOf(myUser.getExp());
			sendMessage(gameResult);

			//exp
			label5 = new JLabel("獲得経験値:" + String.valueOf(myUser.culcAddExp(myUser.getGameResult(), opUser.getLevel())));
			label5.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
			label5.setBounds(500, 430, 250, 40);
			label5.setForeground(Color.WHITE);
			add(label5);


			//背景
			add(backG2);

			break;

		//マッチングルーム(誰もいない)
		case 23:
			 //タイトル
		     label1 = new JLabel("マッチングルーム");
				label1.setFont(new Font("ＭＳ ゴシック", Font.PLAIN, 50));
				x = 300;
				label1.setBounds(x, 10, 400, 100);
				label1.setForeground(Color.WHITE);
				add(label1);


				//プロフィール
				label2 = new JLabel("名前" + myUser.getName() + "レベル:" + myUser.getLevel() + "勝率:" + myUser.getWinPersentage() + "%");

				x = 300;
				label2.setBounds(x, 100, 500, 50);
				label2.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
				add(label2);
				label2.setForeground(Color.WHITE);
				add(label2);

				//ラベル
				label2 = new JLabel("相手がいません。");
				x =350 ;
				y =150;
				label2.setBounds(x, y, 500, 60);
				label2.setFont(new Font(Font.DIALOG, Font.PLAIN, 40));
				label2.setForeground(Color.RED);
				add(label2);

				//更新
				button1 = new JButton("更新");
				button1.setFont(new Font(Font.DIALOG, Font.PLAIN, 30));
				y = WINDOW_Y - 160;
				button1.setBounds(400, y, 150, 40);
				button1.addActionListener(this);
				button1.setActionCommand("Matching:update");
				button1.setBackground(Color.WHITE);
				add(button1);

				button2 = new JButton("マイページへ");
				button2.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
				y = WINDOW_Y - 160;
				button2.setBounds(0, y, 200, 80);
				button2.addActionListener(this);
				button2.setActionCommand("Matching:exit");
				button2.setBackground(Color.WHITE);
				add(button2);

			//背景
			add(backG2);

			break;

		//対局申請後ユーザがいない
		case 24:
			 //タイトル
			label1 = new JLabel("このユーザは現在マッチングルームにいません。");
			x = 100;
			y = 180;
			label1.setBounds(x, y, 800, 50);
			label1.setFont(new Font(Font.DIALOG, Font.PLAIN, 35));
			label1.setForeground(Color.RED);
			add(label1);

			//戻るボタン
			button1 = new JButton("戻る");
			button1.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
			x = 0;
			y = WINDOW_Y - 150 - 10;
			button1.setBounds(x, y, 150, 60);
			button1.addActionListener(this);
			button1.setActionCommand("Mypage:matching");
			button1.setBackground(Color.WHITE);
			add(button1);

			//背景
			add(backG2);

			break;

		//対局申請結果ok
		case 25:
			//ラベル
			label1 = new JLabel("対局申請を許可しました。");
			x = culcCenter(WINDOW_X, 600);
			y = WINDOW_Y / 2 - 100;
			label1.setBounds(x, y, 600, 60);
			label1.setFont(new Font("ＭＳ ゴシック", Font.PLAIN, 50));
			label1.setForeground(Color.WHITE);
			add(label1);

			//デバッグ用ボタン
			button1 = new JButton("対局へ");
			button1.setFont(new Font("Font.DIALOG", Font.PLAIN, 30));
			x = culcCenter(WINDOW_X, 150);
			y += 60 + 60;
			button1.setBounds(x, y, 150, 50);
			button1.addActionListener(this);
			button1.setActionCommand("Matching:game");
			button1.setBackground(Color.WHITE);
			add(button1);

			//背景
			add(backG2);

			break;

		//対局申請を拒否
		case 26:
			//ラベル
			label1 = new JLabel("対局申請を拒否しました。");
			x = culcCenter(WINDOW_X, 600);
			y = WINDOW_Y / 2 - 100;
			label1.setBounds(x, y, 600, 60);
			label1.setFont(new Font("ＭＳ ゴシック", Font.PLAIN, 50));
			label1.setForeground(Color.WHITE);
			add(label1);

			//デバッグ用ボタン
			button1 = new JButton("マッチングルームへ");
			button1.setFont(new Font("Font.DIALOG", Font.PLAIN, 25));
			x = culcCenter(WINDOW_X, 220);
			y += 60 + 30;
			button1.setBounds(x, y, 220, 50);
			button1.addActionListener(this);
			button1.setActionCommand("Mypage:matching");
			button1.setBackground(Color.WHITE);
			add(button1);

			//背景
			add(backG2);

			break;

		//登録(エラー4)
		case 27:
			//エラー
			label6.setText("ユーザ名が長すぎます。");
			label6.setBounds(300, 80, 500, 35);
		    add(label6);

		    add(label1);
		    add(label2);
		    add(label3);
		    add(label4);
		    add(label5);
		    add(text1);
		    add(text2);
		    add(text3);
		    add(button1);
		    add(button2);
			add(backG2);

			break;
		}

		//再描画
		repaint();
	}

	//真ん中を算出
	public int culcCenter(int winSize, int compSize) {
		return (int)(winSize / 2) - (int)(compSize / 2);
	}

	//引数のString型変数がint型化を判断
	public boolean isNumber(String num) {
		try {
			Integer.parseInt(num);
			return true;
		} catch (NumberFormatException e) {
	        return false;
	    }
	}

	//画面を更新
	public void updateDisp(){
		//終了判定
		if(othello.isFinishGame()) {
			changeWindow(17);
		}
		else {
			//オセロ盤をリセット
			for(int i = 0; i < row * row; i++) {
				c.remove(arrayButton[i]);
			}

			//メッセージラベルをリセット
			if(passFlag || faultFlag) {
				label4.setText("");
			}

			//オセロ盤の生成
			arrayButton = new JButton[row * row];//ボタンの配列を作成
			num = 0;
			//x座標：j，y座標：i
			for(int i = 1; i <= row; i++){
				for(int j = 1; j <= row; j++){
					int gridsState = othello.getGridsState(j, i);
					if(gridsState != Othello.S){ //番兵じゃないなら
						//盤面状態に応じたアイコンを設定
						if(gridsState == Othello.B){ arrayButton[num] = new JButton(blackIcon);}
						else if(gridsState == Othello.W){ arrayButton[num] = new JButton(whiteIcon);}
						else if(gridsState == Othello.N){ arrayButton[num] = new JButton(boardIcon);}
						add(arrayButton[num]);//ボタンの配列をペインに貼り付け

						// ボタンを配置する
						x = (num % row) * 45 + 60;
						y = (int) (num / row) * 45 + 60;
						arrayButton[num].setBounds(x, y, 45, 45);//ボタンの大きさと位置を設定する．
						arrayButton[num].addActionListener(this);//マウス操作を認識できるようにする
						arrayButton[num].setActionCommand("Game:" + Integer.toString(othello.changeDemension(j, i)));//ボタンを識別するための名前(番号)を付加する
						num += 1;
					}
				}
			}

			//石の数セット
			myUser.setStone(othello.getStoneNum(myUser.getColor()));
			opUser.setStone(othello.getStoneNum(opUser.getColor()));

			if(!myUser.getColor()) {
				myStr = "●" + rightPad(myUser.getName(), 8) + "  石の数:" + String.valueOf(myUser.getStone());
				opStr = "○" + rightPad(opUser.getName(), 8) + "  石の数:" + String.valueOf(opUser.getStone());
			}
			else {
				myStr = "○" + rightPad(myUser.getName(), 8) + "  石の数:" + String.valueOf(myUser.getStone());
				opStr = "●" + rightPad(opUser.getName(), 8) + "  石の数:" + String.valueOf(opUser.getStone());
			}

			label1.setText(opStr);
			label2.setText(myStr);

			//色表示用ラベル
			String color = "";
			if(othello.getTurn()) {
				color = "●";
			}
			else {
				color = "○";
			}
			label3.setText("現在のターン: " + color);


			//置ける場所がない
			if(!othello.isPutPossibility()) {
				//ターンを変更
				othello.changeTurn();

				//表示
				label4.setText("pass");

				//どちらも置けない
				if(!othello.isPutPossibility()) {
					changeWindow(17);
				}

				//色表示用ラベル
				if(othello.getTurn()) {
					color = "●";
				}
				else {
					color = "○";
				}
				label3.setText("現在のターン: " + color);

				//メッセージラベル
				//パス
				label4.setText("パス");
				passFlag = true;
			}

			//メッセージラベル
			//fault & surrender
			faultFlag = othello.putFlag;
			if(!faultFlag){
				label4.setText("ここには置くことができません");
				othello.putFlag = true;
				faultFlag = true;
			}
			else {
				label4.setText("");
			}

			add(backG2);

			//更新
			repaint();
		}
	}

	public String rightPad(String str, int num) {
		StringBuilder sb = new StringBuilder();

	    sb.append(str);

	    for (int i = str.length(); i < num; i++) {
	    	sb.append(" ");
	    }

	    return sb.toString();
	}

	public static void main(String[] args) {
		Client client = new Client();
	}
}

