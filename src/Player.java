

public class Player {
	private String name = ""; //���[�U��
	private String passWord = "";  //�p�X���[�h
	private int win = 0; //����
	private int defeat = 0; //����
	private int draw = 0; //������
	private int surrender = 0; //����
	private int disconnect = 0;//�ؒf
	private int exp = 0; //�o���l
	private int level = 0; //���x��
	private int total = 0; //��������
	private int winPersentage = 0; //����
	private boolean color = true; //�F
	private int historyNum = 0; //����
	private String historyStr = ""; //�΋Ǘ��𕶎���
	private String[] history; //�΋Ǘ���
	private String gameResult = ""; //��������
	private int stone = 0; //�΂̐�

	//�����Ȃ��R���X�g���N�^
	Player(){
		this.name = "";
	}

	//��������R���X�g���N�^(���[�U��񂩂�e�v�f�ɃZ�b�g)
	Player(String userInfo){
		setResult(userInfo); //���т��Z�b�g
		historyStr = getStrHistory(userInfo); //�΋Ǘ��𕶎���擾
		if(!historyStr.equals("")) {
			historyNum = getHistoryNum(historyStr); //���𐔎擾
			history = new String[historyNum]; //�z���錾
			for(int i = 0; i < historyNum; i++) {
				history[i] = getHistory(historyStr, i);
			}
		}
	}

	//�\��
	public void display() {
		System.out.println(name);
		System.out.println(passWord);
		System.out.println("��:" + String.valueOf(win));
		System.out.println("��:" + String.valueOf(defeat));
		System.out.println("��:" + String.valueOf(draw));
		System.out.println("��:" + String.valueOf(surrender));
		System.out.println("��:" + String.valueOf(disconnect));
		System.out.println("�o:" + String.valueOf(exp));
		System.out.println("��:" + String.valueOf(level));
		System.out.println("��:" + String.valueOf(total));
		System.out.println("��:" + String.valueOf(winPersentage));
		for(int i = 0; i < historyNum; i++) {
			System.out.println("���F" + history[i]);
		}

	}

	//���[�U���C���x���C����
	Player(String name, String exp, String winPersentage){
		this.name = name;
		culcLevel(Integer.parseInt(exp));
		this.winPersentage = Integer.parseInt(winPersentage);
	}

	//�Z�o
	//������
	public void culcTotal() {
		total = win + defeat + draw + surrender;
	}
	//����
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

	//���[�U��񂩂疼�O�C���т��Z�b�g
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

	//���т��Q�b�g
	//��,��,��,��,��,��
	public String getResult() {
		String space = "  ";
		String result = String.valueOf(total) + space +  String.valueOf(win) + space + String.valueOf(defeat) + space +  String.valueOf(draw) + space +  String.valueOf(surrender);
		return result;
	}

	//���[�U��񂩂�΋Ǘ��𕶎�����擾
	public String getStrHistory(String userInfo) {
		String[] data = userInfo.split(",", 9);
		return data[8];
	}

	//�΋ǋL�^�����񂩂�΋Ǘ��𐔂��擾
	public int getHistoryNum(String history) {
		String[] data = history.split(",");
		return data.length / 2;
	}

	//�������猋�ʂ�Ԃ�
	public String getRes(String num) {
		String res = "";

		if(num.equals("1")) {
			res = "��";
		}
		else if(num.equals("2")) {
			res = "��";
		}
		else if(num.equals("3")) {
			res = "��";
		}
		else if(num.equals("4")) {
			res = "��";
		}

		return res;
	}

	//�΋Ǘ��𕶎��񂩂�(num+1)�Ԗڂ̑΋Ǘ������擾
	public String getHistory(String history, int num) {
		String[] data = history.split(",");
		int index = num * 2;
		return data[index] + "," + getRes(data[index + 1]);
	}


	//�Z�b�^�[�C�Q�b�^�[���\�b�h
	//�G���
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

	//���[�U��
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	//�p�X���[�h
	public String getPassWord() {
		return passWord;
	}
	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	//����
	public int getWin() {
		return win;
	}

	//����
	public int getDefeat() {
		return defeat;
	}

	//������
	public int getDraw() {
		return draw;
	}

	//����
	public int getSurrender() {
		return surrender;
	}


	//�o���l
	public int getExp() {
		return exp;
	}
	public int culcAddExp(String result, int opLevel) {
		int addExp = 0; //�ǉ��o���l
		int diff = 0; //���x����

		diff = opLevel - level;

		//����
		if(result.equals("1")) {
			//��������10
			addExp = 10;
			//���背�x��������
			if(diff > 0) {
				addExp += diff * 2;
			}
		}
		//�����C����
		else if(result.equals("2") || result.equals("4")) {
			//��������1
			addExp = 1;
		}
		//������
		else if(result.equals("3")) {
			//��������3
			addExp = 3;
		}

		return addExp;
	}

	public void setExp(String result, int opLevel) {
		exp += culcAddExp(result, opLevel);
	}

	//������
	public int getTotal() {
		return total;
	}

	//����
	public double getWinPersentage() {
		return winPersentage;
	}
	public void setWinPersentage(int winPersentage) {
		this.winPersentage = winPersentage;
	}

	//���x��
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}

	//�F
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

	//��������
	public String getGameResult() {
		return gameResult;
	}
	public void setGameResult(String gameResult) {
		this.gameResult = gameResult;
	}

	//�΂̐�
	public int getStone() {
		return stone;
	}
	public void setStone(int stone) {
		this.stone  = stone;
	}
}
