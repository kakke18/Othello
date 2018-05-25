/*
 * �I�Z���v���O����
 */

public class Othello {
	//�ϐ�
	public static final int N = 0;
	public static final int B = 1;
	public static final int W = 2;
	public static final int S = 3;
	private int row = 8; //�I�Z���Ղ̏c���}�X��
	private int size = 10; //����p�̏c���}�X��
	private boolean turn; //��ԁF��true�C��false
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
	}; //�ǖʏ��
	boolean putFlag = true;

	// �R���X�g���N�^
	public Othello(){
		turn = true; //�������
	}

	//�}�X�����擾
	public int getRow(){
		return row;
	}

	//����p�̃}�X�����擾
	public int getSize(){
		return size;
	}

	//1�������W����x���W���擾
	public int changeX(int i) {
		return i % size;
	}

	//1�������W����y���W���擾
	public int changeY(int i) {
		return (int)(i/10);
	}

	//2��������1�����ւ̍��W�ϊ�
	public int changeDemension(int x, int y){
		return x + y * 10;
	}

	//�^�[�����擾
	public boolean getTurn(){
		return turn;
	}

	//�^�[����ύX
	public void changeTurn(){
		turn = !turn;
	}

	//�^�[������F���擾
	public int getColor(boolean turn) {
		if(turn) {
			return B;
		}
		else {
			return W;
		}
	}

	//�ǖʏ����擾
	public int[] getGrids(){
		return grids;
	}

	//�w�肳�ꂽ2�������W�̋ǖʏ����擾
	public int getGridsState(int i, int j){
		return grids[changeDemension(i, j)];
	}

	//�w�肳�ꂽ1�������W�ɐ΂�u��
	public void putStone(boolean turn, int i){
		int x,y;

		//1������2������
		x = changeX(i);
		y = changeY(i);

		//���@�Ȃ�
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

	//�΂������邩���f
	public boolean isLegal(int i, int j) {
		int dirx, diry, dir;
		int cpos, dpos; //cpos:�����Cdpos:���ꂽ�ʒu
		int ccolor, ocolor; //ccolor:�����̐F�Cocolor:����̐F

		//�����̍��W���擾
		cpos = changeDemension(i, j);

		//�F�擾
		ccolor = getColor(getTurn());
		ocolor = getColor(!getTurn());

		//���̐΁C�ԕ��������ꍇfalse
		if(grids[cpos] != N) {
			return false;
		}

		for(dirx = -1; dirx <= 1; dirx++) {
			for(diry = (-size); diry <= size; diry += size) {
				dir = dirx + diry;

				//�����Ȃ�
				if(dir == 0) { continue;}

				//���ꂽ�ʒu���Z�o
				dpos = cpos + dir;

				while(grids[dpos] == ocolor) {
					dpos += dir;
					if(grids[dpos] == ccolor) { return true; }
				}
			}
		}

		return false;
	}

	//�΂𔽓]
	public void invertStone(int i, int j){
		int dirx, diry, dir;
		int cpos, dpos; //cpos:�����Cdpos:���ꂽ�ʒu
		int ccolor, ocolor; //ccolor:�����̐F�Cocolor:����̐F

		//�����̍��W���擾
		cpos = changeDemension(i, j);

		//�F�擾
		ccolor = getColor(getTurn());
		ocolor = getColor(!getTurn());

		//�����𔽓]
		grids[cpos] = ccolor;

		//�����ȊO�𔽓]
		for(dirx = -1; dirx <= 1; dirx++) {
			for(diry = (-size); diry <= size; diry += size) {
				//�ׂ̍��W�ւ̋���
				dir = dirx + diry;

				//�����Ȃ�
				if(dir == 0) { continue;}

				//���ꂽ�ʒu���Z�o
				dpos = cpos + dir;

				//�ׂ��Ⴄ�F�Ȃ�
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

	//�u����Ƃ��낪���邩���f
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

	//�΂̐����擾
	public int getStoneNum(boolean turn) {
		int sum = 0;
		int color;

		//�F���擾
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

	//�΋ǏI���𔻒f
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

	//���s�𔻒f
	public int judgeWinner() {
		int sumB = getStoneNum(true);
		int sumW = getStoneNum(false);

		System.out.println("���F" + sumB + "���F" + sumW);

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