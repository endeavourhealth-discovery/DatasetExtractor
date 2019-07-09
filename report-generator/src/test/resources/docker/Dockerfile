FROM atmoz/sftp

COPY /sftp /home/foo/
COPY ssh_host_ed25519_key /etc/ssh/ssh_host_ed25519_key
COPY ssh_host_rsa_key /etc/ssh/ssh_host_rsa_key


COPY ssh_host_rsa_key.pub /home/foo/.ssh/keys/id_rsa.pub
