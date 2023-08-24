insert into profile_color
values (1, 'NONE', 0, 0, 0, CURRENT_TIMESTAMP);
insert into profile_color
values (2, '#ffd42c', 255, 212, 44, CURRENT_TIMESTAMP);

insert into topic
values (1, '바다 여행');
insert into topic
values (2, '졸업 여행');

insert into terms
values (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '필수 테스트 약관', '필수 테스트 약관 내용', true);
insert into terms
values (2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '선택 테스트 약관', '선택 테스트 약관 내용', default);
insert into terms
values (3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '필수 테스트 약관2', '필수 테스트 약관 내용', true);
insert into terms
values (4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '선택 테스트 약관2', '선택 테스트 약관 내용', false);
