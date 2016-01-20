-- name: create-user<!
-- creates a new user record
INSERT INTO users
(first_name, last_name, email, passwd)
VALUES (:firstname, :lastname, :email, :passwd)

-- name: update-user!
-- update an existing user record
UPDATE users
SET first_name = :firstname, last_name = :lastname, email = :email
WHERE id = :id

-- name: get-all-users
-- retrieve all users
SELECT * FROM users

-- name: get-user
-- retrieve a user given the id
SELECT * FROM users
WHERE id = :id

-- name: find-user
-- retrieve a user given the email
SELECT * FROM users
WHERE email = :email

-- name: delete-user!
-- delete a user given the id
DELETE FROM users
WHERE id = :id

-- name: create-message<!
-- creates a new message record
INSERT INTO messages
(from_user_id, to_user_id, message, file)
VALUES (:from, :to, :message, :file)

-- name: read-messages
-- retrieve messages for a user
SELECT * FROM messages
WHERE from_user_id = :id
ORDER BY id

-- name: read-user-messages
-- retrieve messages for a user >= msg_id
SELECT * FROM messages
WHERE id > :msgid
AND (to_user_id = :id
     OR from_user_id = :id)

-- name: count-messages-from
-- retrieve message count for a from-user
SELECT COUNT(*) FROM messages
WHERE from_user_id = :id
AND datetime >= :from
AND datetime <= :to
AND message IS NOT NULL
AND trim(message) != ''

-- name: count-messages-to
-- retrieve message count for a to-user
SELECT COUNT(*) FROM messages
WHERE to_user_id = :id
AND datetime >= :from
AND datetime <= :to
AND message IS NOT NULL
AND trim(message) != ''

-- name: count-shares-from
-- retrieve shares count for a from-user
SELECT COUNT(*) FROM messages
WHERE from_user_id = :id
AND datetime >= :from
AND datetime <= :to
AND file IS NOT NULL
AND trim(file) != ''

-- name: count-shares-to
-- retrieve shares count for a to-user
SELECT COUNT(*) FROM messages
WHERE to_user_id = :id
AND datetime >= :from
AND datetime <= :to
AND file IS NOT NULL
AND trim(file) != ''

-- name: create-user-report!
-- create a user-report row
INSERT INTO reports
(user_id, year, week, num_msg_sent, num_msg_recd, num_vid_sent, num_vid_recd)
VALUES (:id, :year, :week, :num_msg_sent, :num_msg_recd, :num_vid_sent, :num_vid_recd)
