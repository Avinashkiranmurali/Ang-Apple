/*
The method getByLocaleOrLanguage() sql is

select word
from naughty_word
where language = '-1'
or locale=userLocale
or language=userLanguage
and locale=-1

We will have 3 locales en_US, fr_CA, -1 and 3 languages en, fr, -1 giving us 9 rows of combination.
So the above query should result in
3 rows for language = -1. 3,6,9 rows.
3 rows for us_EN but one row is shared with -1 language, so 2 new rows. Row 1,2,3
1 row for the language=en and locale=-1. Row no 7.
So total 6 rows.
Rows not fetched 4,5,8
*/
insert into naughty_word (locale, word, pattern, match_whole_word, language)
values ('en_US', 'en_US_en', '', 1, 'en');
insert into naughty_word (locale, word, pattern, match_whole_word, language)
values ('en_US', 'en_US_fr', '', 1, 'fr');
insert into naughty_word (locale, word, pattern, match_whole_word, language)
values ('en_US', 'en_US_-1', '', 1, '-1');

insert into naughty_word (locale, word, pattern, match_whole_word, language)
values ('fr_CA', 'fr_CA_en', '', 1, 'en');
insert into naughty_word (locale, word, pattern, match_whole_word, language)
values ('fr_CA', 'fr_CA_fr', '', 1, 'fr');
insert into naughty_word (locale, word, pattern, match_whole_word, language)
values ('fr_CA', 'fr_CA_-1', '', 1, '-1');

insert into naughty_word (locale, word, pattern, match_whole_word, language)
values ('-1', '-1_en', '', 1, 'en');
insert into naughty_word (locale, word, pattern, match_whole_word, language)
values ('-1', '-1_fr', '', 1, 'fr');
insert into naughty_word (locale, word, pattern, match_whole_word, language)
values ('-1', '-1-1', '', 1, '-1');
