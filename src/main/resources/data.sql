INSERT INTO constraint_config (constraint_name, constraint_type, enabled, weight, threshold, target_part, preferred_days) VALUES
    ('MUST_BE_AVAILABLE_SLOT',         'HARD', true,  1, null, null,     null),
    ('MAX_PER_SLOT',                   'HARD', true,  1,    3, null,     null),
    ('LIMIT_DISTINCT_PARTS',           'HARD', true,  1,    3, null,     null),
    ('INTERVIEWER_MUST_BE_AVAILABLE',  'HARD', true,  1, null, null,     null),
    ('PREFER_SAME_PART',               'SOFT', true,  1, null, null,     null),
    ('PREFER_FULL_SLOTS',              'SOFT', true,  2, null, null,     null),
    ('BONUS_SINGLE_PART',              'SOFT', true,  1, null, null,     null),
    ('PREFER_PART_ON_DAYS_딥러닝',     'SOFT', true,  1, null, '딥러닝', 'SUNDAY,MONDAY'),
    ('PREFER_PART_ON_DAYS_디자인',     'SOFT', true,  1, null, '디자인', 'FRIDAY,MONDAY');
