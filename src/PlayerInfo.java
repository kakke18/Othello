//プレイヤ情報の保持
import java.util.ArrayList;
import java.util.List;
public class PlayerInfo {
	private String playerName;
	private int win;
	private int lose;
	private int draw;
	private int giveup;
	private int shut;
	private int experience;
	private List<String> history;

	//コンストラクタ
	public PlayerInfo(String playerName, int win, int lose,int draw, int giveup, int shut, int experience) {
		this.playerName = playerName;
		this.win = win;
		this.lose = lose;
		this.draw=draw;
		this.giveup = giveup;
		this.shut = shut;
		this.experience = experience;
		this.history = new ArrayList<String>();
	}

	public PlayerInfo(String playerName) {
		this.playerName = playerName;
		this.win = 0;
		this.lose = 0;
		this.draw=0;
		this.giveup = 0;
		this.shut = 0;
		this.experience = 0;
		this.history = new ArrayList<String>();
	}

	public String toString() {
		return playerName + ": " + win + "勝" + lose + "負" + draw +"引き分け"+ giveup + "投了";
	}

	public String getName() {
		return playerName;
	}

	public String writeFile() {
		return playerName + "," + win + "," + lose +","+ draw + "," + giveup + "," + shut + "," + experience;
	}

	public void addwin() {
		win++;
	}

	public void addlose() {
		lose++;
	}
	public void adddraw() {
		draw++;
	}
	public void addgiveup() {
		giveup++;
	}
	public void addshut() {
		shut++;
	}
	public void updataEx(int a) {
		experience = a;
	}
	public void addexperience(int a) {
		//レベルを計算
		int mylevel = 0;
		int opplevel = 0;
		int n = 1;
		for(int i = 0;;i++) {
			n *= 10;
			a = a / n;
			mylevel++;
			if(a <= 0) {
				break;
			}
		}
		n = 1;
		for(int i = 0;;i++) {
			n *= 10;
			a = a / n;
			opplevel++;
			if(a <= 0) {
				break;
			}
		}
		a = (int)(opplevel/5*((2*mylevel+10)*(2*mylevel+10)*Math.sqrt((double)(2*mylevel+10)))/((opplevel+mylevel+10)*(opplevel+mylevel+10)*Math.sqrt((double)(opplevel+mylevel+10)))+1);
		experience += a;
	}

	public int getEx() {
		return experience;
	}

	public int getWp() {
		int n = win + lose + draw + giveup;
		if(n <= 0) {
			n = 1;
		}
		return  (int)Math.ceil(((double)win)/((double)n) * (double)100);
	}
	public void addHistory(String playerName, String result) {
		String s = new String(playerName + ":" + result);
		history.add(s);
	}
	public String showHistory() {
		int limit = history.size()-1;
		int j = 0;
		String s = "history:\n";
		for(int i = limit; i >= 0 && j < 5; i--, j++) {
			s += history.get(i) + "\n";
		}
		return s;
	}
}
