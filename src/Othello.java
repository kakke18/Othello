/*
 * オセロプログラム
 */

public class Othello {
	//変数
	public static final int N = 0;
	public static final int B = 1;
	public static final int W = 2;
	public static final int S = 3;
	private int row = 8; //オセロ盤の縦横マス数
	private int size = 10; //制御用の縦横マス数
	private boolean turn; //手番：黒true，白false
	private int[] grids ={
		S,S,S,S,S,S,S,S,S,S,
		S,N,N,N,N,N,N,N,N,S,
		S,N,N,N,N,N,N,N,N,S,
		S,N,N,N,N,N,N,N,N,S,
		S,N,N,N,B,W,N,N,N,S,
		S,N,N,N,W,B,N,N,N,S,
		S,N,N,N,N,N,N,N,N,S,
		S,N,N,N,N,N,N,N,N,S,
		S,N,N,N,N,N,N,N,N,S,
		S,S,S,S,S,S,S,S,S,S
	}; //局面情報
	boolean putFlag = true;

	// コンストラクタ
	public Othello(){
		turn = true; //黒が先手
	}

	//マス数を取得
	public int getRow(){
		return row;
	}

	//制御用のマス数を取得
	public int getSize(){
		return size;
	}

	//1次元座標からx座標を取得
	public int changeX(int i) {
		return i % size;
	}

	//1次元座標からy座標を取得
	public int changeY(int i) {
		return (int)(i/10);
	}

	//2次元から1次元への座標変換
	public int changeDemension(int x, int y){
		return x + y * 10;
	}

	//ターンを取得
	public boolean getTurn(){
		return turn;
	}

	//ターンを変更
	public void changeTurn(){
		turn = !turn;
	}

	//ターンから色を取得
	public int getColor(boolean turn) {
		if(turn) {
			return B;
		}
		else {
			return W;
		}
	}

	//局面情報を取得
	public int[] getGrids(){
		return grids;
	}

	//指定された2次元座標の局面情報を取得
	public int getGridsState(int i, int j){
		return grids[changeDemension(i, j)];
	}

	//指定された1次元座標に石を置く
	public void putStone(boolean turn, int i){
		int x,y;

		//1次元→2次元へ
		x = changeX(i);
		y = changeY(i);

		//合法なら
		if(isLegal(x, y)){
			putFlag = true;
			if(turn){
				grids[i] = B;
			}
			else{
				grids[i] = W;
			}
			invertStone(x, y);
			changeTurn();
		}
		else {
			System.out.println("fault");
			putFlag = false;
		}
	}

	//石がおけるか判断
	public boolean isLegal(int i, int j) {
		int dirx, diry, dir;
		int cpos, dpos; //cpos:自分，dpos:離れた位置
		int ccolor, ocolor; //ccolor:自分の色，ocolor:相手の色

		//自分の座標を取得
		cpos = changeDemension(i, j);

		//色取得
		ccolor = getColor(getTurn());
		ocolor = getColor(!getTurn());

		//他の石，番兵だった場合false
		if(grids[cpos] != N) {
			return false;
		}

		for(dirx = -1; dirx <= 1; dirx++) {
			for(diry = (-size); diry <= size; diry += size) {
				dir = dirx + diry;

				//自分なら
				if(dir == 0) { continue;}

				//離れた位置を算出
				dpos = cpos + dir;

				while(grids[dpos] == ocolor) {
					dpos += dir;
					if(grids[dpos] == ccolor) { return true; }
				}
			}
		}

		return false;
	}

	//石を反転
	public void invertStone(int i, int j){
		int dirx, diry, dir;
		int cpos, dpos; //cpos:自分，dpos:離れた位置
		int ccolor, ocolor; //ccolor:自分の色，ocolor:相手の色

		//自分の座標を取得
		cpos = changeDemension(i, j);

		//色取得
		ccolor = getColor(getTurn());
		ocolor = getColor(!getTurn());

		//自分を反転
		grids[cpos] = ccolor;

		//自分以外を反転
		for(dirx = -1; dirx <= 1; dirx++) {
			for(diry = (-size); diry <= size; diry += size) {
				//隣の座標への距離
				dir = dirx + diry;

				//自分なら
				if(dir == 0) { continue;}

				//離れた位置を算出
				dpos = cpos + dir;

				//隣が違う色なら
				if(grids[dpos] == ocolor) {
					do{
						dpos += dir;
					}while(grids[dpos] == ocolor);

					if(grids[dpos] == ccolor) {
						do {
							grids[dpos] = ccolor;
							dpos -= dir;
						}while(grids[dpos] == ocolor);
					}
				}
			}
		}
	}

	//置けるところがあるか判断
	public boolean isPutPossibility(){
		for(int i = 1; i <= row; i++) {
			for(int j = 1; j <= row; j++) {
				if(isLegal(i,j)) {
					return true;
				}
			}
		}

		return false;
	}

	//石の数を取得
	public int getStoneNum(boolean turn) {
		int sum = 0;
		int color;

		//色を取得
		color = getColor(turn);

		for(int i= 0; i <= row; i++) {
			for(int j = 0; j <= row; j++) {
				if(grids[changeDemension(i, j)] == color) {
					sum += 1;
				}
			}
		}

		return sum;
	}

	//対局終了を判断
	public boolean isFinishGame() {
		int sumB = getStoneNum(true);
		int sumW = getStoneNum(false);

		if(sumB + sumW == row * row) {
			return true;
		}
		else {
			return false;
		}
	}

	//勝敗を判断
	public int judgeWinner() {
		int sumB = getStoneNum(true);
		int sumW = getStoneNum(false);

		System.out.println("黒：" + sumB + "白：" + sumW);

		if(sumB > sumW) {
			return B;
		}
		else if(sumB < sumW) {
			return W;
		}
		else {
			return 3;
		}
	}
}