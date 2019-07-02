package constant;

public interface Constant {
	// 클라이언트 > 서비스
	// 대기실 접속
	public static final String ENTERROBBY = "010001";
	// 대기실 퇴장
	public static final String EXITROBBY = "019001";
	// 닉네임 입력
	public static final String WRITENICKNAME = "010501";
	// 대화방 만들기
	public static final String CREATEROOM = "011001";
	// 전체 할일 출력
	public static final String PRINTTODO = "040001";
	// 할일 추가
	public static final String ADDTODO = "041001";
	// 할일 제거
	public static final String DELTODO = "041501";

	// 서비스 > 클라이언트
	// 방 정보를 List에 뿌리기
	public static final String ROOMLIST = "013002";
	// 대기실 인원 정보
	public static final String ROBBYWAITINFO = "013502";
	// 개설된 방의 타이틀 제목 얻기
	public static final String GETROOMNAME = "021002";
	// 클라이언트 채팅 화면을 초기화
	public static final String CLEARMSG = "035002";

	// 양쪽 사용
	// (대기실에서) 대화방 인원정보
	public static final String ROBBYUSERINFO = "015003";
	// (대화방에서) 대화방 인원정보
	public static final String ROOMUSERINFO = "015553";
	// 대화방 입장
	public static final String ENTERROOM = "020003";
	// 대화방 퇴장
	public static final String EXITROOM = "025003";
	// 클라이언트 채팅 화면에 메시지를 출력
	public static final String PRINTMSG = "030003";

	// 로그인 관련
	// 서버에 ID 비밀번호 전송
	public static final String SENDIDPW = "080001";
	// 회원 가입
	public static final String SIGNUP = "085001";
	// IDPW 일치
	public static final String LOGINOK = "081002";
	// IDPW 불일치
	public static final String LOGINFAIL = "082002";
	// 가입하려는 ID가 이미 존재
	public static final String SIGNUPFAIL = "085502";

	// 파일 입출력 관련
	// 방 정보 저장
	public static final String ROOMS = "ROOMS.txt";
	// 계정 정보 저장
	public static final String USERS = "USERS.txt";
}
