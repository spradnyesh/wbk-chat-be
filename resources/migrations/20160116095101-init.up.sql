CREATE TABLE users (
id SERIAL PRIMARY KEY,
first_name VARCHAR(30),
last_name VARCHAR(30),
email VARCHAR(50),
passwd VARCHAR(300));

CREATE TABLE messages (
id SERIAL PRIMARY KEY,
from_user_id SMALLINT references users(id),
to_user_id SMALLINT references users(id),
message VARCHAR(300),
datetime TIMESTAMP);

CREATE TABLE reports (
id SERIAL PRIMARY KEY,
user_id SMALLINT references users(id),
year SMALLINT,
week SMALLINT,
num_msg_sent SMALLINT,
num_msg_recd SMALLINT,
num_vid_sent SMALLINT,
num_vid_recd SMALLINT);
