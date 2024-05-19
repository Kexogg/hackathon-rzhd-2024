import React from 'react';

type SearchbarProps = React.DetailedHTMLProps<React.InputHTMLAttributes<HTMLInputElement>, HTMLInputElement>;

const Searchbar = (props: SearchbarProps) => {
    return (
        <input className={
            'border border-gray-300 rounded-lg p-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent'
        } {...props} type={'search'}/>
    );
};

export default Searchbar;
