

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server{
	private Socket socket[];
    private int port; // サーバの待ち受けポート
    private int [] online; //オンライン状態管理用配列
    private String [] player;//プレイヤ番号とプレイヤ名を保存
    private int [][] match;//試合相手
    private PrintWriter [] out; //データ送信用オブジェクト
    private Receiver [] receiver; //データ受信用オブジェクト
    private Map<String, String> accountMap; //ユーザ名とパスワードを保持するハッシュマップ
    private Map<String, Integer> playerNoMap;//プレイヤ名とプレイヤNOを保持するハッシュマップ
    private Map<Integer, Integer> matchingMap;//マッチング中の相手
    private List<PlayerInfo> playerInfo;//プレイヤ情報
    private Map<String, PlayerInfo> infoMap;
    private Date [] date;
    private boolean bl = false;
    private Status sta;

    //コンストラクタ
    public Server(int port) { //待ち受けポートを引数とする
    	socket = new Socket [100];//ソケット作成
        this.port = port; //待ち受けポートを渡す
        out = new PrintWriter [100]; //データ送信用オブジェクト
        receiver = new Receiver [100]; //データ受信用オブジェクト
        player = new String[100];//プレイヤNOとプレイヤ名
        match = new int[100][2];//マッチング
        online = new int[100]; //オンライン状態管理配列
        accountMap = new HashMap<String,String>(); //ユーザ名とパスワードを保持するハッシュマップ
        playerNoMap = new HashMap<String,Integer>();
        matchingMap = new HashMap<Integer, Integer>();
        playerInfo = new ArrayList<PlayerInfo>();
        infoMap = new HashMap<String, PlayerInfo>();
        date = new Date[100];

        sta = new Status();


        //テスト用
       /* player[50] = "testuser1";
        player[51] = "testuser2";
        player[52] = "testuser3";

        online[50] = 2;
        online[51] = 2;
        online[52] = 2;
        online[53] = 2;

        playerNoMap.put("testuser1", 50);
        playerNoMap.put("testuser2", 51);
        playerNoMap.put("testuser3", 52);
     */

    }
    //実験

    //ステータス確認用
    class Status extends Thread {
    	private InputStreamReader sisr;
    	private BufferedReader br;

    	Status() {
    		sisr = new InputStreamReader(System.in);
    		br = new BufferedReader(sisr);
    	}

    	public void run() {
    		try {
    			while(true) {
    				if(sisr.read() != -1) {
    					printStatus();
    				}
    			}
    		}catch(IOException e) {
    			System.err.println("ステータスの出力に失敗しました:" + e);
    		}
    	}

    }

    class Shut extends Thread {
    	private int playerNo;
    	//public boolean alive;
    	private Receiver receive;


    	Shut(int playerNo, Receiver receive) {
    		this.playerNo = playerNo;
    		//alive = true;//生きている
    		this.receive = receive;
    	}

    	public void run() {
    		int counter = 0;
    		while(true) {
    			synchronized(this) {
    				try {
    					wait(60000);
    					if(counter == 0) {
    						counter++;
    						receive.forwardMessage("ping", playerNo);
    					} else {
    						System.out.println("a: " + counter);
    						receive.alive = false;
    						break;//切断された
    					}

    				} catch(InterruptedException e) {
    					e.printStackTrace();
    					counter = 0;
    					System.out.println("interruption: " + counter);
    				}
    			}
    		}
    		System.out.println("out");
    		receive.playerShut(playerNo);
    	}

    }

    // データ受信用スレッド(内部クラス)
    class Receiver extends Thread {
        private InputStreamReader sisr; //受信データ用文字ストリーム
        private BufferedReader br; //文字ストリーム用のバッファ
        private int playerNo; //プレイヤを識別するための番号
        private Shut shut;
        private boolean alive = true;
        private boolean shutF = true;
        private int c1 = 0;


        // 内部クラスのReceiverのコンストラクタ
        Receiver (Socket socket, int playerNo){
            try{
                this.playerNo = playerNo; //プレイヤ番号を渡す
                sisr = new InputStreamReader(socket.getInputStream());
                br = new BufferedReader(sisr);
                online[playerNo] = 0;//初期状態
                shut = new Shut(playerNo,this);
                //online[playerNo] = 3;//初期状態
                //match[1] = 2;
                //match[2] = 1;
               // shut = new Shut(playerNo);

            } catch (IOException e) {
                System.err.println("データ受信中にエラーが発生しました: " + e);
            }
        }
        // 内部クラスReceiverのメソッド
        public void run(){

        	shut.start();



        		try{
        			while(true) {// データを受信し続ける
        			String inputLine = br.readLine();//データを一行ぶん読み込む
        			if (inputLine != null){ //データを受信したら
        				receiveMessage(inputLine,playerNo);
        			}
        			}
        		} catch(IOException e) {//そのほかのエラー
        			e.printStackTrace();
        			System.err.println(player[playerNo] + "との接続でエラーが発生しました");
                    //online[playerNo] = 0; //プレイヤの接続状態を更新する
                    //player[playerNo] = null;
        			printStatus(); //接続状態を出力する
        			//receiver[playerNo].start();//どうなることやら
        		} catch(Exception e) {
        			e.printStackTrace();
        			System.err.println(player[playerNo] + "との接続でエラーが発生しました");
        			printStatus(); //接続状態を出力する
        			playerShut(playerNo);
        		}

        }

        public boolean receiveMessage(String msg, int playerNo) {
        	//try {
        		System.out.println("receive: " + "from:" + player[playerNo] + "   " + msg);
        		int f = 0;
        		//新規登録
        		if(msg.contains("Registration:")) {
        			online[playerNo] = 0;
        			f = 1;
        			String[] s = msg.split(":");
        			receivePlayersInfo(s[1],playerNo,f);
        			f = 0;
        			//return true;
        		}

    	    	 //ログイン
        		else if(msg.contains("Login:")) {
    	    		 online[playerNo] = 0;
    	    		 f = 2;
    	    		 String[] s = msg.split(":");
    	    		 receivePlayersInfo(s[1],playerNo,f);
    	    		 f = 0;
    	    		 //return true;
    	    	 }

        		//Mypage
        		else if(msg.contains("Mypage:")) {
        			if(msg.contains("matching")) {
    	    			online[playerNo] = 2;//マッチング状態に変換
    	    			date[playerNo] = new Date();//日付を取得
    	    			getMatchingInfo(playerNo);//敵の情報を送信
    	    			//return true;
        			} else if(msg.contains("logout")) {
        				playerShut(playerNo);
        				//shut.alive = false;
        				shut.interrupt();
        			}
        			return false;
        		}

    	    	 //マッチング
        		else if(msg.contains("Matching:")) {
    	    		 //online[playerNo] = 2;//マッチング状態に変換
    	    		 if(msg.contains("update")) {//マッチング情報のアップデートをリクエストしているならば
    	    			 getMatchingInfo(playerNo);
    	    		 } else if(msg.contains("exit")) {//マイページに戻る
    	    			 online[playerNo]--;//マイページに戻る
    	    			 forwardMessage("Login:success:" + makeSentence(playerNo),playerNo);
    	    		 } else {//他プレイヤの名前が送られてきたら
    	    			 //String[] s = msg.split(":");
    	    			 setGame(msg,playerNo);//マッチング周り
    	    		 }
    	    		 //return true;
        		}

    	    	//対局
        		else if(msg.contains("Game:")) {
        			online[playerNo] = 3;
        			game(playerNo,msg);
        			//return true;
        		}

        		//対局終了
        		else if(msg.contains("GameFinish:")) {
        			String s[] = msg.split(":");

        			String s2[] = s[1].split(",");
        			if(s2.length > 2) {//対局結果が送られてきたならば
        				int result = Integer.parseInt(s2[2]);//対戦結果
        				int experience = Integer.parseInt(s2[3]);//更新済みの経験値
        				rewrite(result,experience,playerNo);//対局結果の更新
        					//試し用
           					//forwardMessage("GameFinish:" + player[match[playerNo]] + "," + player[playerNo] + ",4,3",match[playerNo]);//ためし
        					//online[playerNo] = 4;//対局終了画面
        					//したの命令は後でもとに戻す
        					int n = match[playerNo][0];
        					if(match[n][1] == 1) {//相手がすでに処理を終了しているならば
        						match[n][0] = 0;
        						match[playerNo][0] = 0;//対戦中の配列から外す
        					} else {//相手がまだ更新していないならば
        						match[playerNo][1] = 1;
        					}

        					if(!shutF) {

        						online[n] = 0;//初期状態に戻す
        		            	String s1 = player[n];
        		            	playerNoMap.remove(s1);//プレイヤ名とプレイヤNoの関係を切る
        		            	player[n] = null;//プレイヤ名とプレイヤNoの関係を切る
        					}


        			} else if(msg.contains("mypage")){
        				online[playerNo] = 1;
        				forwardMessage("Login:success:" + makeSentence(playerNo),playerNo);//もう一度プレイヤの情報を送信
        			}
        			//return true;
        		} else if(msg.contains("pong")) {//接続が絶たれていない場合には
        			shut.interrupt();
        		}
        		return true;
        }

        //メッセージの送信 playerNoは送信相手
        public void forwardMessage(String msg, int playerNo){ //メッセージの転送
        	//try {
        		out[playerNo].println(msg);
        		out[playerNo].flush();
        		System.out.println("send: to:" + player[playerNo] + "   " + msg);
        	/*} catch(Exception e) {
        		e.printStackTrace();
        		System.err.println("メッセージの送信に失敗しました");
        	}*/
        }


        //新規登録,ログイン
        //新規登録、ログイン用の文字列の取得
        public void receivePlayersInfo(String str, int playerNo, int f) {
            String s[];
            s = str.split(",");
           // try {
                if(f == 1) {//新規登録ならば
                	if(s.length < 3) {
                		forwardMessage("Registration:blank",playerNo);
                	} else {
                		register(s[0],s[1],s[2],playerNo);
                    }
                } else {//ログインならば
                	if(s.length < 2) {
                		forwardMessage("Login:blank",playerNo);
                	} else {
                		login(s[0],s[1],playerNo);
                	}
                }
           /* } catch(Exception e) {//ここの文字列確認
                //forwardMessage("false",playerNo);
            	e.printStackTrace();
            }*/
        }

        //新規登録
        public void register(String name, String pass1, String pass2, int playerNo) {

            if(passwordCheck(pass1,pass2)) {
                if(accountMap.get(name) != null) {//プレイヤ名が被っている場合
                	forwardMessage("Registration:alreadyExists",playerNo);
                } else if(name.length() >= 9) {
                	forwardMessage("Registration:tooLong",playerNo);

                } else {//新規登録
                    accountMap.put(name, pass1);//プレイヤ名とパスワードをマップに保存
                    writeFile("\n" + name + "," + pass1, "account.txt");//ファイルへの書き込み
                    forwardMessage("Registration:success",playerNo);
                    try {
                    	File file = new File(name+".txt");
                    	if(!file.exists()){
                    		file.createNewFile();
                    	}
                    }catch(IOException e) {
                    	e.printStackTrace();
                    	System.err.println(name + "のファイルの作成に失敗しました");
                    }
                    PlayerInfo newp = new PlayerInfo(name);
                    playerInfo.add(newp);
                    infoMap.put(name, newp);
                    writeFile("\n" + newp.writeFile(),"playerInfo.txt");//ファイルへの書き込み
                }
            } else {//確認用アドレスが一致しない場合
                forwardMessage("Registration:notMatch",playerNo);
            }
        }

        //ログイン
        public void login(String name, String pass, int playerNo) {
            String str = checkPassword(name,pass);
            if(playerNoMap.containsKey(name)) {//同時ログインは禁止
            	forwardMessage("Login:noUser",playerNo);//ログイン失敗
            } else if(str.contains("success")) {
                player[playerNo] = name;
                playerNoMap.put(name, playerNo);//プレイヤ名とプレイヤナンバーをマップ上に一致させる
                online[playerNo] = 1;//オンライン接続状態を更新する
                forwardMessage("Login:success:" + makeSentence(playerNo),playerNo);
            } else {
            	forwardMessage(str,playerNo);
            }
        }

        public String makeSentence(int playerNo) {
            String str;
            str = player[playerNo];
           // str += "," + accountMap.get(str);
            str += "," + getPlayerInfo(playerNo);
            str += getHistory(playerNo);
            return str;
        }

        public String makeSentence2(int playerNo) {
            String str;
            str = player[playerNo];//プレイヤ名を取得
            System.out.println(str);
           // str += "," + accountMap.get(str);
    		PlayerInfo info = infoMap.get(str);
            str += "," + info.getEx()+","+info.getWp();
            return str;
        }

        //パスワードの一致の確認
        public boolean passwordCheck(String pass1, String pass2) {
            if(pass1.equals(pass2)) {
                return true;
            } else {
                return false;
            }
        }

        //プレイヤ名とパスワードの一致の確認
        public String checkPassword(String name,String password) {
            String pass;
            String msg;
            pass = accountMap.get(name);//マップから登録されたパスワードを取り出す
            if(pass == null) {//ユーザが存在しない場合
                msg = "Login:noUser";
            } else {
                if(pass.equals(password)) {
                    msg = "Login:success";
                } else {
                    msg = "Login:notMatch";
                }
            }
            return msg;
        }

        //マッチング関係
        //マッチング周り
        public synchronized void setGame(String str, int playerNo) {
            int num = 0;
           try {
    	        if(str.contains("offerResult:")) {//申請の結果
    	        	if(str.contains("ok")) {
    	        	     num = matchingMap.get(playerNo);//申請中のマップから申請してきた相手のナンバーを取得
    	        	     forwardMessage("Matching:offerResult:ok",num);//申請者に結果を報告

    	        	     match[playerNo][0] = num;//対戦中の配列に登録
    	        	     match[playerNo][1] = 0;//対戦中の配列に登録
    	        	     match[num][0] = playerNo;//対戦相手側も登録
    	        	     match[num][1] = 0;//対戦相手側も登録
    	                 matchingMap.remove(playerNo);//申請中のマップからは取り除く(自分の番号だけでよい)
    	                 //sendColor(playerNo);//自分に色情報を送る
    	                 //sendColor(num);//相手に色情報を送る
    	                 online[playerNo] = 3;//状態を対局中に変更
    	                 online[num] = 3;//相手の状態も変更
    	                 forwardMessage("Matching:game:" + sendColor(num),num);//相手にメッセージを送信
    	                 forwardMessage("Matching:game:" + sendColor(playerNo),playerNo);//受理者にもメッセージを送信

    	        	} else if(str.contains("fault")) {//申請を拒否
    	        		//forwardMessage("Matching:offerResult:fault",playerNo);//相手にメッセージを送信
    	        		forwardMessage("Matching:offerResult:fault",matchingMap.get(playerNo));//相手にメッセージを送信
    		            matchingMap.remove(playerNo);//申請中のマップから取り除く
    		            matchingMap.remove(num);
    	        	}
    	        } else if(str.contains("offer:")){//プレイヤの名前が送られてくる場合(申請する)
    	        	System.out.println(str);
    	        	String[] s1 = str.split(":");
    	        	//System.out.println(s1[1]);
    	        	String[] s2 = s1[2].split(",");//名前を取り出す
    	        		try {
    	        		num = playerNoMap.get(s2[0]);//プレイヤ名からプレイヤナンバーを取得


    	        		if(online[num] == 2) {//申請相手がマッチング中ならば
    	        			forwardMessage("Matching:" + s1[1] + ":" + s2[0], playerNo);//送信者にも情報を送信
    	        			forwardMessage("Matching:offerAccept:" + makeSentence2(playerNo),num);//メッセージを相手に送信
    	        			matchingMap.put(num,playerNo);//相手と自分をマッチング中のマップに追加
    	        			matchingMap.put(playerNo, num);
    	        		} else {//相手がマッチング中でないならば
    	        			forwardMessage("Matching:offerNoUser",playerNo);//メッセージが帰ってくる
    	        		}

    	            }catch(java.lang.NullPointerException e) {//切断している可能性もある
    	            	forwardMessage("Matching:offerNoUser",playerNo);
    	            }
    	        } else if(str.contains("offerCancel")){//申請がキャンセルされた場合
    	        	System.out.println("offerCancel");
    	        	num = matchingMap.get(playerNo);//申請していた人のナンバーを取得
    	        	forwardMessage("Matching:offerCancel",playerNo);//本人にも送り直す
    	        	forwardMessage("Matching:offerCancel",num);//キャンセルを相手方に伝える
    	        	matchingMap.remove(num);//申請中のマップからは取り除く
    	        	matchingMap.remove(playerNo);
    	        } else if(str.contains("game")) {
    	        	  num = matchingMap.get(playerNo);//申請中のマップから申請した相手の番号を取得
    	        	  match[playerNo][0] = num;//対戦中の配列に登録
    	              match[num][0] = playerNo;//対戦相手側も登録
    	              matchingMap.remove(playerNo);//申請中のマップからは取り除く
    	              matchingMap.remove(num);
    	              //sendColor(playerNo);//自分に色情報を送る
    	              //sendColor(num);//相手に色情報を送る
    	              online[playerNo] = 3;//状態を対局中に変更
    	              online[num] = 3;//相手の状態も変更
    	              forwardMessage("Matching:game:" + sendColor(num),num);//相手にメッセージを送信
    	              forwardMessage("Matching:game:" + sendColor(playerNo),playerNo);//受理者にもメッセージを送信
    	        }
           }catch(ArrayIndexOutOfBoundsException e) {

           }
        }

        //マッチングのための情報を送信する
        public void getMatchingInfo(int playerNo) {
            //forwardMessage("Matching:infostart",playerNo);//送信開始を連絡
        	String str = "Matching:enter:";
        	int count = 0;
            for(int i = 1; i < online.length; i++) {
                if(online[i] == 2 && i != playerNo) {//マッチング状態にあるプレイヤの情報を送信、自分を除く
                    str += makeSentence2(i) + ",";
                    count++;
                    if(count >= 5) break;
                }
            }
            forwardMessage(str,playerNo);//送信終了を連絡
        }

        //対局
        public void game(int playerNo, String msg) {
        	String[] s = msg.split(":");
        	if(msg.contains("surrender")) {//投了を受け付け
        		int opp = match[playerNo][0];//対局相手のプレイヤNOを取得
        		System.out.println("game");
        		//if(c1 == 0) {//1回目の送信ならば
        			forwardMessage("Game:surrenderAccept",opp);//敵に投了情報を送信
        		//} else {//2回目以降の受け取りはしない
        			//c1 = 0;
        		//}
        		//forwardMessage("Game:surrenderAccept",playerNo);//敵に投了情報を送信
        		//match[playerNo] = 0;//対戦中の配列から外す
        	} else {//局面情報の送信
    			forwardMessage("Game:" + s[1],match[playerNo][0]);
    			forwardMessage("Game:" + s[1],playerNo);
    		}
        }

        public String getPlayerInfo(int playerNo) {//指定されたプレイヤNOの対局情報を返信
        	String name = player[playerNo];//ナンバーからプレイヤの名前を出す
        	//System.out.println(name);
        	PlayerInfo info = infoMap.get(name);
        	return info.writeFile();
        }

        //先手後手情報の送信
        public String sendColor(int playerNo){ //先手後手情報の送信
        	boolean f = true;
        	int a = date[playerNo].compareTo(date[match[playerNo][0]]);
            if(a == -1) {//自分の入場時間の方が早い場合
                return "black";
            } else if(a == 1){//相手の方が早い場合
                return "white";
            } else {//同時入場の場合
            	if(f == true) {
            		f = false;
            		return "black";
            	} else {
            		f = true;
            		return "white";
            	}
            }
            //forwardMessage(date[playerNo].toString(), match[playerNo]);//対戦相手に相手のマッチング入場時間を送る

        }

        public void playerShut(int playerNo) {
        	if(online[playerNo] == 3) {//対局中ならば
        		System.out.println("playerShut");
        		forwardMessage("Game:surrenderAccept",match[playerNo][0]);//投了情報を送信
        		PlayerInfo p = playerInfo.get(playerNo);//投了した人の情報;
        		rewrite(4,p.getEx(),playerNo);//結果を書き込み
        		receiver[match[playerNo][0]].shutF = false;
        	} else if(online[playerNo] == 2) {//マッチング中ならば
        		matchingMap.remove(matchingMap.get(playerNo));//マッチング関係を解除する
        		matchingMap.remove(playerNo);
        		online[playerNo] = 0;//初期状態に戻す
            	String s = player[playerNo];
            	playerNoMap.remove(s);//プレイヤ名とプレイヤNoの関係を切る
            	player[playerNo] = null;//プレイヤ名とプレイヤNoの関係を切る
        	} else {
        		online[playerNo] = 0;//初期状態に戻す
        		String s = player[playerNo];
        		playerNoMap.remove(s);//プレイヤ名とプレイヤNoの関係を切る
        		player[playerNo] = null;//プレイヤ名とプレイヤNoの関係を切る
        	}
        }
    }


    // メソッド
    //クライアントの接続(サーバ起動)
    public void acceptClient(){ //クライアントの接続(サーバの起動)

        try {

            System.out.println("サーバが起動しました");
            ServerSocket ss = new ServerSocket(port); //サーバソケットを用意
            int i = 1; //プレイヤ1から始める
            while (true) {
                socket[i] = ss.accept(); //新規接続を受け付ける
                System.out.println("サーバが起動しました");
                out[i] = new PrintWriter(socket[i].getOutputStream(),true);//データ送信用オブジェクト
                receiver[i] = new Receiver(socket[i],i);//データ受信用オブジェクト
                receiver[i].start();//スレッドを動かす
                i++;

            }

        } catch (Exception e) {
            System.err.println("ソケット作成時にエラーが発生しました:" + e);
        }
    }


    //接続状態の確認
    public void printStatus(){
        System.out.println("1:マイページ, 2:マッチング状態, 3:対局中");
        for(int i = 1; i < online.length; i++) {
        	int sta;
        	if((sta = online[i]) != 0) {
        	System.out.println(player[i] + ":" + sta);
        	}
        }
    }



    //ファイル登録関係
    //ファイルへの追記
    public static void writeFile(String str, String filename){
        try {
            FileWriter fr = new FileWriter(filename, true);
            BufferedWriter bw = new BufferedWriter(fr);
            PrintWriter pw = new PrintWriter(bw);

            pw.print(str);//ファイルへ追記
            pw.close();//ファイルを閉じる
        } catch(IOException e) {
            e.printStackTrace();
            System.err.println("ファイルへの書き込みに失敗しました");
        }
    }

    //ファイルへの書き込み
    public void writeAll() {
        try {
            FileWriter fr = new FileWriter("playerInfo.txt");
            BufferedWriter bw = new BufferedWriter(fr);
            PrintWriter pw = new PrintWriter(bw);
            pw.print(playerInfo.get(0).writeFile());//ファイルへ追記
            for(int i = 1; i < playerInfo.size(); i++) {
            	pw.print("\n" + playerInfo.get(i).writeFile());//ファイルへ追記
            }
            pw.close();//ファイルを閉じる
        } catch(IOException e) {
            e.printStackTrace();
            System.err.println("ファイルへの書き込みに失敗しました");
        }
    }

    //ファイルからアカウント(プレイヤ名、パスワード)の読み込み
    //プレイヤのアカウント情報のファイルからの取得
    public void getPlayersAccount() {
        try {
            FileReader fr = new FileReader("Account.txt");//Account.txt
            BufferedReader br = new BufferedReader(fr);
            String str;
            while((str = br.readLine()) != null) {
            	String s[] = str.split(",", 0);
            	if(s.length == 2) {
            		accountMap.put(s[0], s[1]);//プレイヤ名、パスワードをハッシュマップに追加
            	}
            }
            br.close();
            for(String key:accountMap.keySet()) {
                System.out.println(accountMap.get(key));
            }
        }catch(IOException ioe) {
            ioe.printStackTrace();
            System.err.println("ファイルからのアカウントの取り出しに失敗しました");
        }
    }

    //ファイルからのプレイヤ情報の取り出し
    public void setPlayersInfo() {
        try {
            FileReader fr = new FileReader("playerinfo.txt");
            BufferedReader br = new BufferedReader(fr);
            String str;


            while((str = br.readLine()) != null) {

                System.out.println(str);
                String s[] = new String[6];
                s = str.split(",", 0);
                if(s.length == 7) {
                	PlayerInfo newp = new PlayerInfo(s[0],Integer.parseInt(s[1]),Integer.parseInt(s[2]),Integer.parseInt(s[3]),Integer.parseInt(s[4]),Integer.parseInt(s[5]),Integer.parseInt(s[6]));
                	playerInfo.add(newp);
                	infoMap.put(s[0],newp);//名前とクラスのマップに追加
                }
                //newp = null;
            }
            //System.out.println(playerInfo);

            /*for(String key: infoMap.keySet()) {
                PlayerInfo p = infoMap.get(key);
                System.out.println(p.toString());
            }*/
            br.close();
        }catch(IOException ioe) {
            ioe.printStackTrace();
            System.err.println("ファイルからのプレイヤ情報の取り出しに失敗しました");
        }
    }

    //切断した場合の記録の更新
   /* public void rewrite(int result, int playerNo) {
    	//try {
    		int opponum = match[playerNo];//対戦相手のプレイヤNO
    		PlayerInfo info = infoMap.get(player[playerNo]);//本人のプレイヤ情報
    		//PlayerInfo oppoinfo = infoMap.get(player[opponum]);//相手のプレイヤ情報
    		info.addshut();
//    	} catch(NullPointerException e) {

  //  	}
    }*/

    //試合結果の反映
    public synchronized void rewrite(int result,int experience, int playerNo) {
    	//try {
    	if(match[playerNo][0] != 0) {
    		int opponum = match[playerNo][0];//対戦相手のプレイヤNO
    		PlayerInfo info = infoMap.get(player[playerNo]);//本人のプレイヤ情報
    		PlayerInfo oppoinfo = infoMap.get(player[opponum]);//相手のプレイヤ情報


	        if(result == 1) {//勝ちならば
	            info.addwin();//勝ち数を更新
	            info.updataEx(experience);
	            //info.addexperience(oppoinfo.getEx());//経験値を更新
	        } else if(result ==2) {
	            info.addlose();//負け数を更新
	            info.updataEx(experience);
	        } else if(result == 3) {
	            info.adddraw();//引き分け数を更新
	            info.updataEx(experience);
	        } else if(result == 4) {//投了数を更新
	        	info.addgiveup();
	        	info.updataEx(experience);
	        } /*else if(result == 5){//切断
	        	info.addshut();//切断数を更新
	        }*/
	      /*  while(bl == true) {
	            try {
	                wait();
	            } catch(InterruptedException e) {}
	        }*/

	        makeHistory(info.getName(),oppoinfo.getName(),result);
	        writeAll();
    	}
	            /*if(str.equals("win")) {//勝ちならば
	                writeFile("\n" + info.getName() + "," + oppoinfo.getName(), "History.txt"); 直す前

	            } else if(str.equals("lose")) {

	                writeFile("\n" + oppoinfo.getName() + "," + info.getName(), "History.txt");
	            }*/

	        //bl = true;
	        //notifyAll();

    }

    //プレイヤの接続が切れた場合に行うこと

    //試し
    /*public String getHistory3(int playerNo) {
    	int count = 0;
    	String str = "";
    	try {
    		String playername = player[playerNo];
    		File file = new File(playername+".txt");
    		BufferedReader br = new BufferedReader(new FileReader(file));
    		Stack<String> lines = new Stack<String>();
    		String line = br.readLine();
    		while(line != null) {
    			lines.push(line);
    			line = br.readLine();
    		}

    		while(!lines.empty() && count < 5) {
    			str += lines.pop();
    		}
    	}catch(Exception e) {

    	}
    }*/
    //5行取り出す
    /*public String getHistory(int playerNo) {

    	String playname=player[playerNo];
    	File file=new File(playname+".txt");
    	List<String> result = new ArrayList<String>();
    	long count = 0;
    	String str=null;
    	if (!file.exists() || file.isDirectory() || !file.canRead())//読み取り失敗の場合
    		return null;
    	RandomAccessFile fileRead = null;
    	try {
    		fileRead = new RandomAccessFile(file, "r");
    		long length = fileRead.length();
    		if (length == 0L) { //履歴はないなら、直接にreturn
    			return ",";
    		} else {
    			long pos = length - 1;
    			while (pos > 0) {
    				pos--;
    				fileRead.seek(pos);
    				if (fileRead.readByte() == '\n') {
    					String line = fileRead.readLine();
    					result.add(line);
    					System.out.println(line);
    					count++;
    					if (count == 5L)
    						break;
    				}
    			}
    			if (pos == 0) {
    				fileRead.seek(0);
    				result.add(fileRead.readLine());
    			}
    			str=result.get(0);
    			System.out.println(length);
    			if(length< 5L){
    				for(long i=1;i<length;i++){
    					int s=(int)i;
    					str +=","+result.get(s);
    				}
    			}else{
    				for(long i=1;i<5L;i++){
    					int s=(int)i;
    					str +=","+result.get(s);
    				}
    			}
    		}
     }
     catch (IOException e)
     {
         e.printStackTrace();
     }
     finally
     {
         if (fileRead != null)
         {
             try
             {
                 fileRead.close();
             }
             catch (Exception e)
             {
             }
         }
     }
     return  str;
 }*/
   /* public String getHistory(int playerNo) {
    	String playname=player[playerNo];
        Map<Integer,String> dataMap = new HashMap<Integer,String>();
        FileReader file2;
        int num = 0;
        String str=",";
        try {
            file2 = new FileReader(playname+".txt");
            BufferedReader in = new BufferedReader(file2);
            while (in.ready()) {
                dataMap.put((num % 5), in.readLine());
                num++;
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (num < 5) {
            for (int i = 0; i < num; ++i) {
                str+=dataMap.get(i)+",";
            }
        } else {
            int key = num % 5;
            for (int i = key; i < 5; ++i) {
                str+=dataMap.get(i)+",";
            }
            for (int i = 0; i < key; ++i) {
                str+=dataMap.get(i)+",";
            }
        }

        return str;
    }*/
    //ファイルからプレイヤの対戦履歴を取り出す(全試合出力)
     public String getHistory(int playerNo) {
    	try {
	        String playname=player[playerNo];
	        File file=new File(playname+".txt");
	        InputStreamReader reader=new InputStreamReader(new FileInputStream(file));
	        BufferedReader br=new BufferedReader(reader);
	        String line="";
	        String str=",";
	        //line=br.readLine();
	        while((line = br.readLine())!=null){
	        	//line=br.readLine();
	        	str+=line+",";
	        }
	       return str;
    	} catch(IOException e) {
    		e.printStackTrace();
    		System.err.println("ファイルからプレイヤ履歴の取り出しに失敗しました");
    		return null;
    	}
    }

    //ユーザーごとに各対戦相手との対局情報(プレイヤー名、相手、対局情報)  (1つのプレイヤーに対して１つのファイル)
    public void makeHistory(String playername,String oppo,int result) {
    	try {
	        File file = new File(playername+".txt");
	        if(!file.exists()){
	            file.createNewFile();
	        }
	        // write
	        FileWriter fw = new FileWriter(file, true);
	        BufferedWriter bw = new BufferedWriter(fw);
	        bw.write(oppo+","+result);
	        bw.newLine();
	        bw.flush();
	        bw.close();
	        fw.close();
    	} catch(IOException e) {
    		e.printStackTrace();
    		System.err.println("対局記録の書き込みに失敗しました");
    	}

    }


    public static void main(String[] args){ //main
    	//try {
    		//int port = Integer.parseInt(args[0]);

    		//Server server = new Server(port);
    		Server server = new Server(10000);
    		server.getPlayersAccount();
    		server.setPlayersInfo();
    		server.sta.start();
    		server.acceptClient();
    	/*} catch(Exception e) {
    		e.printStackTrace();
    	}*/
    }
}



