package kr.weit.odya.support

import kr.weit.odya.service.ALLOW_FILE_FORMAT_LIST

const val EXIST_USER_ERROR_MESSAGE = "$TEST_USERNAME: 이미 존재하는 회원입니다"

const val ALREADY_REGISTER_USER_ERROR_MESSAGE = "$TEST_USERNAME: 이미 등록된 사용자입니다"

const val NOT_EXIST_USER_ERROR_MESSAGE = "$TEST_USERNAME: 존재하지 않는 회원입니다"

const val EXIST_NICKNAME_ERROR_MESSAGE = "$TEST_NICKNAME: 이미 존재하는 닉네임입니다"

const val EXIST_EMAIL_ERROR_MESSAGE = "$TEST_EMAIL: 이미 존재하는 이메일입니다"

const val NOT_EXIST_AUTHENTICATED_EMAIL_ERROR_MESSAGE = "인증된 이메일이 존재하지 않습니다"

const val EXIST_PHONE_NUMBER_ERROR_MESSAGE = "$TEST_PHONE_NUMBER: 이미 존재하는 전화번호입니다"

const val NOT_EXIST_AUTHENTICATED_PHONE_NUMBER_ERROR_MESSAGE = "인증된 전화번호가 존재하지 않습니다"

const val EXIST_PLACE_REVIEW_ERROR_MESSAGE = "이미 리뷰를 작성한 장소입니다."

const val NOT_EXIST_PLACE_REVIEW_ERROR_MESSAGE = "존재하지 않는 장소 리뷰입니다."

const val FORBIDDEN_PLACE_REVIEW_ERROR_MESSAGE = "작성자만 수정할 수 있습니다."

const val ALREADY_FOLLOW_ERROR_MESSAGE = "$TEST_OTHER_USER_ID: 해당 유저를 이미 팔로우 중 입니다"

val NOT_ALLOW_FILE_FORMAT_ERROR_MESSAGE = "프로필 사진은 ${ALLOW_FILE_FORMAT_LIST.joinToString()} 형식만 가능합니다"

const val NOT_EXIST_ORIGIN_FILE_NAME_ERROR_MESSAGE = "원본 파일 이름이 존재하지 않습니다"

const val NOT_EXIST_PROFILE_COLOR_ERROR_MESSAGE = "프로필 색상이 존재하지 않습니다"

const val NOT_EXIST_NONE_PROFILE_COLOR_ERROR_MESSAGE = "프로필 색상(NONE)이 존재하지 않습니다"

const val SOMETHING_ERROR_MESSAGE = "something error message"

const val INVALID_DELETE_DEFAULT_PROFILE_ERROR_MESSAGE = "기본 프로필은 삭제할 수 없습니다"

const val DELETE_NOT_EXIST_PROFILE_ERROR_MESSAGE = "$TEST_PROFILE_PNG: Object Storage에 존재하지 않는 파일입니다"

const val NOT_FOUND_FAVORITE_PLACE_ERROR_MESSAGE = "해당 장소는 관심 장소 등록되어있지 않습니다."

const val EXIST_FAVORITE_PLACE_ERROR_MESSAGE = "$TEST_PLACE_ID: 해당 장소는 이미 관심 장소입니다"

const val NOT_FOUND_REQUIRED_TERMS_ERROR_MESSAGE = "$TEST_TERMS_ID : 필수 약관에 동의하지 않았습니다."

const val NOT_FOUND_TERMS_ERROR_MESSAGE = "$TEST_NOT_EXIST_TERMS_ID : 해당 약관이 존재하지 않습니다."
