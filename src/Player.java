

public class Player {
	private String name = ""; //ユーザ名
	private String passWord = "";  //パスワード
	private int win = 0; //勝ち
	private int defeat = 0; //負け
	private int draw = 0; //引分け
	private int surrender = 0; //投了
	private int disconnect = 0;//切断
	private int exp = 0; //経験値
	private int level = 0; //レベル
	private int total = 0; //総試合数
	private int winPersentage = 0; //勝率
	private boolean color = true; //色
	private int historyNum = 0; //履歴数
	private String historyStr = ""; //対局履歴文字列
	private String[] history; //対局履歴
	private String gameResult = ""; //試合結果
	private int stone = 0; //石の数

	//引数なしコンストラクタ
	Player(){
		this.name = "";
	}

	//引数ありコンストラクタ(ユーザ情報から各要素にセット)
	Player(String userInfo){
		setResult(userInfo); //成績をセット
		historyStr = getStrHistory(userInfo); //対局履歴文字列取得
		if(!historyStr.equals("")) {
			historyNum = getHistoryNum(historyStr); //履歴数取得
			history = new String[historyNum]; //配列を宣言
			for(int i = 0; i < historyNum; i++) {
				history[i] = getHistory(historyStr, i);
			}
		}
	}

	//表示
	public void display() {
		System.out.println(name);
		System.out.println(passWord);
		System.out.println("勝:" + String.valueOf(win));
		System.out.println("負:" + String.valueOf(defeat));
		System.out.println("引:" + String.valueOf(draw));
		System.out.println("投:" + String.valueOf(surrender));
		System.out.println("切:" + String.valueOf(disconnect));
		System.out.println("経:" + String.valueOf(exp));
		System.out.println("レ:" + String.valueOf(level));
		System.out.println("総:" + String.valueOf(total));
		System.out.println("率:" + String.valueOf(winPersentage));
		for(int i = 0; i < historyNum; i++) {
			System.out.println("歴：" + history[i]);
		}

	}

	//ユーザ名，レベル，勝率
	Player(String name, String exp, String winPersentage){
		this.name = name;
		culcLevel(Integer.parseInt(exp));
		this.winPersentage = Integer.parseInt(winPersentage);
	}

	//算出
	//総試合
	public void culcTotal() {
		total = win + defeat + draw + surrender;
	}
	//勝率
	public void culcWinPersentage() {
		if(total == 0) {
			winPersentage = 0;
		}
		else {
			winPersentage = (int)win * 100 / total;
		}
	}

	public int needExp(int level){
		if(level == 1){
			return 0;
		}

		double num = 10;
		while(level > 2){
			num += num * 1.1;
			level -= 1;
		}

		return (int)num;
	}
	public void culcLevel(int exp){
		if(exp < 10){
			level = 1;
		}
		else{
			level = 2;

			while(exp > needExp(level)){
				level += 1;
			}
		}
	}

	//ユーザ情報から名前，成績をセット
	public void setResult(String userInfo) {
		String[] data;
		data = userInfo.split(",");

		name = data[0];
		win = Integer.parseInt(data[2]);
		defeat = Integer.parseInt(data[3]);
		draw = Integer.parseInt(data[4]);
		surrender = Integer.parseInt(data[5]);
		exp = Integer.parseInt(data[7]);
		culcTotal();
		culcWinPersentage();
		culcLevel(exp);
	}

	//成績をゲット
	//総,勝,負,引,投,切
	public String getResult() {
		String space = "  ";
		String result = String.valueOf(total) + space +  String.valueOf(win) + space + String.valueOf(defeat) + space +  String.valueOf(draw) + space +  String.valueOf(surrender);
		return result;
	}

	//ユーザ情報から対局履歴文字列を取得
	public String getStrHistory(String userInfo) {
		String[] data = userInfo.split(",", 9);
		return data[8];
	}

	//対局記録文字列から対局履歴数を取得
	public int getHistoryNum(String history) {
		String[] data = history.split(",");
		return data.length / 2;
	}

	//数字から結果を返す
	public String getRes(String num) {
		String res = "";

		if(num.equals("1")) {
			res = "勝";
		}
		else if(num.equals("2")) {
			res = "負";
		}
		else if(num.equals("3")) {
			res = "引";
		}
		else if(num.equals("4")) {
			res = "投";
		}

		return res;
	}

	//対局履歴文字列から(num+1)番目の対局履歴を取得
	public String getHistory(String history, int num) {
		String[] data = history.split(",");
		int index = num * 2;
		return data[index] + "," + getRes(data[index + 1]);
	}


	//セッター，ゲッターメソッド
	//敵情報
	public void setOpponent(String opInfo) {
		String[] data = opInfo.split(",");
		name = data[0];
		exp = Integer.parseInt(data[1]);
		culcLevel(exp);
		winPersentage = (int)Double.parseDouble(data[2]);
	}
	public String getOpponent() {
		return name + "  " + String.valueOf(level) + "  " + String.valueOf(winPersentage);
	}

	//ユーザ名
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	//パスワード
	public String getPassWord() {
		return passWord;
	}
	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	//勝ち
	public int getWin() {
		return win;
	}

	//負け
	public int getDefeat() {
		return defeat;
	}

	//引分け
	public int getDraw() {
		return draw;
	}

	//投了
	public int getSurrender() {
		return surrender;
	}


	//経験値
	public int getExp() {
		return exp;
	}
	public int culcAddExp(String result, int opLevel) {
		int addExp = 0; //追加経験値
		int diff = 0; //レベル差

		diff = opLevel - level;

		//勝ち
		if(result.equals("1")) {
			//勝ったら10
			addExp = 10;
			//相手レベルが高い
			if(diff > 0) {
				addExp += diff * 2;
			}
		}
		//負け，投了
		else if(result.equals("2") || result.equals("4")) {
			//負けたら1
			addExp = 1;
		}
		//引分け
		else if(result.equals("3")) {
			//引分けは3
			addExp = 3;
		}

		return addExp;
	}

	public void setExp(String result, int opLevel) {
		exp += culcAddExp(result, opLevel);
	}

	//総試合
	public int getTotal() {
		return total;
	}

	//勝率
	public double getWinPersentage() {
		return winPersentage;
	}
	public void setWinPersentage(int winPersentage) {
		this.winPersentage = winPersentage;
	}

	//レベル
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}

	//色
	public boolean getColor() {
		return color;
	}
	public void setColor(boolean color) {
		this.color = color;
	}
	public void setColor(String color) {
		if(color.equals("black")) {
			this.color = true;
		}
		else {
			this.color = false;
		}
	}

	//試合結果
	public String getGameResult() {
		return gameResult;
	}
	public void setGameResult(String gameResult) {
		this.gameResult = gameResult;
	}

	//石の数
	public int getStone() {
		return stone;
	}
	public void setStone(int stone) {
		this.stone  = stone;
	}
}
