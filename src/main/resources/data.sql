INSERT INTO constraint_config (constraint_name, constraint_type, enabled, weight, threshold, target_part, preferred_days) VALUES
    ('MUST_BE_AVAILABLE_SLOT',         'HARD', true,  1, null, null,     null),
    ('MAX_PER_SLOT',                   'HARD', true,  1,    3, null,     null),
    ('LIMIT_DISTINCT_PARTS',           'HARD', true,  1,    3, null,     null),
    ('INTERVIEWER_MUST_BE_AVAILABLE',  'SOFT', true,  1, null, null,     null),
    ('PREFER_SAME_PART',               'SOFT', true,  1, null, null,     null),
    ('PREFER_FULL_SLOTS',              'SOFT', true,  2, null, null,     null),
    ('BONUS_SINGLE_PART',              'SOFT', true,  1, null, null,     null),
    ('PREFER_PART_ON_DAYS_백엔드',         'SOFT', true,  1, null, '백엔드',         'MONDAY,TUESDAY'),
    ('PREFER_PART_ON_DAYS_Web프론트엔드', 'SOFT', true,  1, null, 'Web 프론트엔드', 'MONDAY,TUESDAY'),
    ('PREFER_PART_ON_DAYS_App프론트엔드', 'SOFT', true,  1, null, 'App 프론트엔드', 'MONDAY,TUESDAY'),
    ('PREFER_PART_ON_DAYS_딥러닝',        'SOFT', true,  1, null, '딥러닝',         'MONDAY,TUESDAY'),
    ('PREFER_PART_ON_DAYS_데이터분석',    'SOFT', true,  1, null, '데이터분석',     'MONDAY,TUESDAY'),
    ('PREFER_PART_ON_DAYS_디자인',        'SOFT', true,  1, null, '디자인',         'MONDAY,TUESDAY');
